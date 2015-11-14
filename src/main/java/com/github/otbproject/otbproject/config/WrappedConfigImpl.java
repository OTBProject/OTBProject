package com.github.otbproject.otbproject.config;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.util.JsonHandler;
import com.github.otbproject.otbproject.util.ThreadUtil;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class WrappedConfigImpl<T> implements WrappedConfig<T> {
    protected static final BlockingDeque<Runnable> UPDATE_DEQUE = new LinkedBlockingDeque<>();
    protected static final ExecutorService UPDATE_SERVICE;

    static {
        UPDATE_SERVICE = ThreadUtil.getSingleThreadExecutor("config-updater");
        UPDATE_SERVICE.execute(() -> {
            // Loops forever, because there is not currently any scenario in which it
            // is sensible to interrupt it (as that would prevent all further updates
            // and edits)
            while (true) {
                Uninterruptibles.takeUninterruptibly(UPDATE_DEQUE).run();
            }
        });
    }

    protected T config;
    protected final Class<T> tClass;
    protected final String path;
    private boolean needsUpdate = false;

    public WrappedConfigImpl(Class<T> tClass, String path, Supplier<T> defaultConfigSupplier) {
        this.tClass = tClass;
        this.path = path;
        config = JsonHandler.readValue(path, tClass).orElseGet(defaultConfigSupplier);
        writeToFile();
    }

    @Override
    public <R> R get(Function<T, R> function) {
        return function.apply(config);
    }

    @Override
    public <R> R getExactly(Function<T, R> function) throws ExecutionException, InterruptedException {
        FutureTask<R> futureTask = new FutureTask<>(() -> function.apply(config));
        UPDATE_DEQUE.addLast(futureTask);
        return futureTask.get();
    }

    @Override
    public <R> Optional<R> getExactlyAsOptional(Function<T, R> function) {
        try {
            return Optional.of(getExactly(function));
        } catch (InterruptedException e) {
            App.logger.catching(e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            App.logger.catching(e);
        }
        return Optional.empty();
    }

    @Override
    public void edit(Consumer<T> consumer) {
        UPDATE_DEQUE.addLast(() -> {
            updateIfNeeded();
            consumer.accept(config);
            writeToFile();
        });
    }

    protected void writeToFile() {
        JsonHandler.writeValue(path, config);
    }

    @Override
    public void updateLater() {
        queueUpdate(false);
    }

    @Override
    public void updateAndAwait() {
        final FutureTask<Void> task = new FutureTask<>(() -> {
            updateIfNeeded(false);
            return null;
        });
        UPDATE_DEQUE.addFirst(() -> needsUpdate = true);
        UPDATE_DEQUE.addLast(task);
        try {
            Uninterruptibles.getUninterruptibly(task);
        } catch (ExecutionException e) {
            App.logger.catching(e);
        }
    }

    protected void queueUpdate(boolean logUpdate) {
        UPDATE_DEQUE.addFirst(() -> {
            if (!needsUpdate) {
                needsUpdate = true;
                UPDATE_DEQUE.addLast(() -> updateIfNeeded(logUpdate));
            }
        });
    }

    protected void updateIfNeeded() {
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
}
