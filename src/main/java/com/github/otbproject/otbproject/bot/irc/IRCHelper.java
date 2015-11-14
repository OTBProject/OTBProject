package com.github.otbproject.otbproject.bot.irc;

class IRCHelper {
    private IRCHelper() {}

    public static String getIrcChannelName(String channel) {
        return "#" + channel;
    }

    public static String getInternalChannelName(String channel) {
        return channel.substring(1);
    }
}
