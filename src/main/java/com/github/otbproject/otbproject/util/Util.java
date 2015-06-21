package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Util {
    public static ExecutorService getSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setUncaughtExceptionHandler((t, e) -> {
                            App.logger.error("Thread crashed: " + t.getName());
                            App.logger.catching(e);
                        })
                        .build()
        );
    }

    public static ExecutorService getSingleThreadExecutor(String nameFormat) {
        return Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat(nameFormat)
                        .setUncaughtExceptionHandler((t, e) -> {
                            App.logger.error("Thread crashed: " + t.getName());
                            App.logger.catching(e);
                        })
                        .build()
        );
    }
}
