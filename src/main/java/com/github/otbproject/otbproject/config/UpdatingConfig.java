package com.github.otbproject.otbproject.config;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.util.JsonHandler;
import com.github.otbproject.otbproject.util.ThreadUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UpdatingConfig<T> extends WrappedConfig<T> {
    private static final FileAlterationMonitor MONITOR;

    static {
        MONITOR = new FileAlterationMonitor(200); // 5 times per second
        MONITOR.setThreadFactory(new ThreadFactoryBuilder()
                .setNameFormat("file-alteration-monitor")
                .setUncaughtExceptionHandler(ThreadUtil.UNCAUGHT_EXCEPTION_HANDLER)
                .build()
        );
        try {
            MONITOR.start();
        } catch (Exception e) {
            App.logger.error("Failed to start file change monitor");
            App.logger.catching(e);
        }
    }

    private volatile boolean monitoring = false;
    private boolean needsUpdate = false;
    private boolean needsWrite = false;
    private final FileAlterationObserver observer;

    private UpdatingConfig (Class<T> tClass, String directory, String fileName, Supplier<T> defaultConfigSupplier) {
        super(tClass, (directory + File.separator + fileName), defaultConfigSupplier);
        observer = new FileAlterationObserver(new File(directory), file -> file.getName().equals(fileName));
    }

    private void init() {
        observer.addListener(new CustomFileAlterationListener<>(this));
        try {
            observer.initialize();
        } catch (Exception e) {
            App.logger.error("Failed to initialize observer for file: " + path);
            App.logger.catching(e);
        }
    }

    public static <T> UpdatingConfig<T> create(Class<T> tClass, String directory, String fileName, Supplier<T> defaultConfigSupplier) {
        UpdatingConfig<T> updatingConfig = new UpdatingConfig<>(tClass, directory, fileName, defaultConfigSupplier);
        updatingConfig.init();
        return updatingConfig;
    }

    /**
     * Start monitoring for file changes
     * @return {@code false} if already monitoring, {@code true} otherwise
     */
    public synchronized boolean startMonitoring() {
        if (monitoring) {
            return false;
        }
        MONITOR.addObserver(observer);
        monitoring = true;
        queueUpdate(false);
        App.logger.debug("Started monitoring file: " + path);
        return true;
    }

    /**
     * Stop monitoring for file changes
     * @return {@code false} if not already monitoring, {@code true} otherwise
     */
    public synchronized boolean stopMonitoring() {
        if (!monitoring) {
            return false;
        }
        MONITOR.removeObserver(observer);
        monitoring = false;
        App.logger.debug("Stopped monitoring file: " + path);
        return true;
    }

    @Override
    public void edit(Consumer<T> consumer) {
        UPDATE_DEQUE.addLast(() -> {
            updateIfNeeded();
            consumer.accept(config);
            writeToFile();
            needsWrite = false;
        });
    }

    private void onFileChange() {
        queueUpdate(true);
    }

    private void onFileDelete() {
        UPDATE_DEQUE.addFirst(() -> {
            if (!needsWrite) {
                needsWrite = true;
                UPDATE_DEQUE.addLast(this::writeIfNeeded);
            }
        });
    }

    private void queueUpdate(boolean logUpdate) {
        UPDATE_DEQUE.addFirst(() -> {
            if (!needsUpdate) {
                needsUpdate = true;
                UPDATE_DEQUE.addLast(() -> this.updateIfNeeded(logUpdate));
            }
        });
    }

    private void updateIfNeeded() {
        updateIfNeeded(true);
    }

    private void updateIfNeeded(boolean logUpdate) {
        if (needsUpdate) {
            needsUpdate = false;
            if (updateFromFile() && logUpdate) {
                App.logger.debug("Updated changed file: " + path);
            }
        }
    }

    private boolean updateFromFile() {
        Optional<T> optional = JsonHandler.readValue(path, tClass);
        config = optional.orElse(config);
        return optional.isPresent();
    }


    private void writeIfNeeded() {
        if (needsWrite) {
            writeToFile();
            needsWrite = false;
            App.logger.debug("Regenerated deleted file: " + path);
        }
    }

    private static class CustomFileAlterationListener<T> extends FileAlterationListenerAdaptor {
        private final UpdatingConfig<T> updatingConfig;

        public CustomFileAlterationListener(UpdatingConfig<T> updatingConfig) {
            this.updatingConfig = updatingConfig;
        }

        @Override
        public void onFileChange(File file) {
            updatingConfig.onFileChange();
        }

        @Override
        public void onFileDelete(File file) {
            updatingConfig.onFileDelete();
        }
    }
}
