package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.gui.GuiApplication;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Watcher {
    private static final int EXCEPTION_LIMIT = 5;
    private static final int DUPLICATE_LIMIT = 3;

    private Watcher() {}

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("watcher")
                    .setUncaughtExceptionHandler(((t, e) -> {
                        App.logger.error("Watcher thread crashed");
                        App.logger.error("Please report watcher crash to the developers");
                        App.logger.catching(e);
                        if (Control.Graphics.present()) {
                            GuiApplication.errorAlert("Watcher Error", "OTB watcher has encountered an error");
                        }
                    }))
                    .build()
    );

    private static int numExceptions = 0;

    public static void logException() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        final String stackTraceElement = (stackTrace.length > 1) ? stackTrace[1].toString() : "unknown location";
        EXECUTOR_SERVICE.execute(() -> {
            numExceptions++;
            App.logger.error("Unexpected exception [" + numExceptions + "] caught at " + stackTraceElement);
            if (numExceptions == 1) {
                App.logger.error("OTB has experienced an internal error");
                App.logger.error("Please report this problem to the developers");
                if (Control.Graphics.present()) {
                    GuiApplication.errorAlert("Internal Error", "OTB has experienced an internal error");
                }
            }
            if ((numExceptions >= EXCEPTION_LIMIT) && (numExceptions < EXCEPTION_LIMIT + DUPLICATE_LIMIT)) {
                App.logger.error("OTB has experienced multiple internal errors");
                App.logger.error("Please report this problem to the developers");
                if (Control.Graphics.present()) {
                    GuiApplication.errorAlert("Internal Error", "OTB has experienced multiple internal errors");
                }
            }
        });
    }



}
