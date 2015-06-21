package com.github.otbproject.otbproject.channel;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException() {
        super();
    }

    public ChannelNotFoundException(String message) {
        super(message);
    }
}
