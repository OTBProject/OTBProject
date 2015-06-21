package com.github.otbproject.otbproject.bot;

public class BotInitException extends Exception {
    public BotInitException() {
        super("Unable to properly create bot");
    }

    public BotInitException(String message) {
        super(message);
    }

    public BotInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
