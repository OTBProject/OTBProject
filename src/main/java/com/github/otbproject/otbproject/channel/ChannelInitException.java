package com.github.otbproject.otbproject.channel;

public class ChannelInitException extends Exception {
    public ChannelInitException(String channel) {
        super("Unable to properly initialize channel: " + channel);
    }

    public ChannelInitException(String channel, String errMsg) {
        super("Unable to properly initialize channel '" + channel + "'. Error: " + errMsg);
    }
}
