package com.github.otbproject.otbproject.messages.send;

public class MessageOut implements Comparable<MessageOut> {
    private final String message;
    private final MessagePriority priority;

    public MessageOut(String message) {
        this(message, MessagePriority.DEFAULT);
    }

    public MessageOut(String message, MessagePriority priority) {
        this.message = message;
        this.priority = priority;
    }

    public String getMessage() {
        return message;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public int compareTo(MessageOut messageOut) {
        if (messageOut == null) {
            throw new NullPointerException("Cannot compare to a MessageOut which is null.");
        }
        return Integer.valueOf(priority.getValue()).compareTo(messageOut.getPriority().getValue());
    }
}
