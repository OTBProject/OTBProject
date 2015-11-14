package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {
    public static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER;

    private ThreadUtil() {}

    static {
        UNCAUGHT_EXCEPTION_HANDLER = (t, e) -> {
            App.logger.error("Thread crashed: " + t.getName());
            App.logger.catching(e);
            Watcher.logException();
        };
        Thread.setDefaultUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
    }

    public static ExecutorService getSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER)
                        .build()
        );
    }

    public static ExecutorService getSingleThreadExecutor(String nameFormat) {
        return Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat(nameFormat)
                        .setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER)
                        .build()
        );
    }

    public static void interruptIfInterruptedException(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
