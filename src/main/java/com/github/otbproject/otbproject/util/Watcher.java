package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.gui.GuiApplication;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Watcher {
    private static final int CRASH_LIMIT = 5;
    private static final int DUPLICATE_LIMIT = 3;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("watcher")
                    .setUncaughtExceptionHandler(((t, e) -> {
                        App.logger.error("Watcher thread crashed");
                        App.logger.error("Please report watcher crash to the developers");
                        App.logger.catching(e);
                        if (Bot.Graphics.present()) {
                            GuiApplication.errorAlert("Watcher Error", "OTB watcher has encountered an error");
                        }
                    }))
                    .build()
    );

    private static int threadCrashes = 0;

    public static void logThreadCrash() {
        EXECUTOR_SERVICE.execute(() -> {
            threadCrashes++;
            if ((threadCrashes >= CRASH_LIMIT) && (threadCrashes < CRASH_LIMIT + DUPLICATE_LIMIT)) {
                App.logger.error("OTB has experienced multiple internal errors");
                App.logger.error("Please report this problem to the developers");
                if (Bot.Graphics.present()) {
                    GuiApplication.errorAlert("Internal Error", "OTB has experienced multiple internal errors");
                }
            }
        });
    }



}
