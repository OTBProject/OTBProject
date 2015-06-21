package com.github.otbproject.otbproject.messages.internal;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.gui.GuiApplication;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InternalMessageSender {
    private static final ExecutorService EXECUTOR_SERVICE;
    public static final String DESTINATION_PREFIX = "internal:";
    public static final String CLI = "cli";

    static {
        EXECUTOR_SERVICE = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                        .setNameFormat("Internal-Message-Sender-%d")
                        .setUncaughtExceptionHandler((t, e) -> {
                            App.logger.error("Thread crashed: " + t.getName());
                            App.logger.catching(e);
                        })
                        .build()
        );
    }

    public static void send(String destination, String message, String source) {
        // Ensure method call returns quickly
        EXECUTOR_SERVICE.execute(() -> {
            switch (destination) {
                case CLI:
                    sendToCli(message, source);
                    break;
            }
        });
    }

    private static void sendToCli(String message, String source) {
        App.logger.debug("Sending to CIL from <" + source + ">: " + message);
        GuiApplication.addInfo("<" + source + "> " + message);
    }
}
