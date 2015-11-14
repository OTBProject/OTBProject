package com.github.otbproject.otbproject.messages.internal;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.gui.GuiApplication;

public class InternalMessageSender {
    public static final String DESTINATION_PREFIX = "internal:";
    public static final String CLI = "cli";
    public static final String TERMINAL = "terminal";

    private InternalMessageSender() {}

    public static void send(String destination, String message, String source) {
        switch (destination) {
            case CLI:
                sendToCli(message, source);
                break;
            case TERMINAL:
                sendToTerminal(message, source);
                break;
            default:
                App.logger.error("Invalid internal destination: " + destination);
        }
    }

    private static void sendToCli(String message, String source) {
        App.logger.debug("Sending to CLI from <" + source + ">: " + message);
        GuiApplication.addInfo("<" + source + "> " + message);
    }

    private static void sendToTerminal(String message, String source) {
        System.out.println("<" + source + "> " + message);
    }
}
