package com.github.otbproject.otbproject.channel;

import com.github.otbproject.otbproject.bot.Control;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Deprecated
public class Channels {
    private Channels() {}

    public static boolean in(String channelName) {
        return Control.bot().channelManager().in(channelName);
    }

    public static Optional<Channel> get(String channel) {
        return Optional.ofNullable(Control.bot().getChannels().get(channel));
    }

    public static Channel getOrThrow(String channel) throws ChannelNotFoundException {
        return get(channel).orElseThrow(ChannelNotFoundException::new);
    }

    public static boolean join(String channelName) {
        return Control.bot().channelManager().join(channelName);
    }

    public static boolean join(String channelName, EnumSet<JoinCheck> checks) {
        return Control.bot().channelManager().join(channelName, checks);
    }

    public static boolean leave(String channelName) {
        return Control.bot().channelManager().leave(channelName);
    }

    public static Set<String> list() {
        return Control.bot().channelManager().list();
    }

    public static boolean isBotChannel(String channel) {
        return channel.equalsIgnoreCase(Control.bot().getUserName());
    }

    public static boolean isBotChannel(Channel channel) {
        return isBotChannel(channel.getName());
    }
}
