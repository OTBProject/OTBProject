package com.github.otbproject.otbproject.channel;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.command.parser.ResponseParserUtil;
import com.github.otbproject.otbproject.config.*;
import com.github.otbproject.otbproject.fs.Setup;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Channels {
    private static final Lock lock = new ReentrantLock();

    public static boolean in(String channelName) {
        Optional<Channel> optional = get(channelName);
        return optional.isPresent() && optional.get().isInChannel();
    }
    
    public static Optional<Channel> get(String channel) {
        return Optional.ofNullable(Bot.getBot().getChannels().get(channel));
    }

    public static Channel getOrThrow(String channel) throws ChannelNotFoundException {
        return get(channel).orElseThrow(ChannelNotFoundException::new);
    }

    public static boolean join(String channelName) {
        return join(channelName, true);
    }

    public static boolean join(String channelName, boolean checkValidChannel) {
        channelName = channelName.toLowerCase();
        App.logger.info("Attempting to join channel: " + channelName);
        boolean isBotChannel;
        BotConfig botConfig;

        lock.lock();
        try {
            if(in(channelName)){
                App.logger.info("Failed to join channel: " + channelName + ". Already in channel");
                return false;
            }

            isBotChannel = channelName.equals(Bot.getBot().getUserName());

            // Check whitelist/blacklist
            botConfig = Configs.getBotConfig();
            ChannelJoinSetting channelJoinSetting = botConfig.getChannelJoinSetting();
            if (!isBotChannel) {
                if (channelJoinSetting == ChannelJoinSetting.WHITELIST) {
                    if (!BotConfigHelper.isWhitelisted(botConfig, channelName)) {
                        App.logger.info("Failed to join channel: " + channelName + ". Not whitelisted.");
                        return false;
                    }
                } else if (channelJoinSetting == ChannelJoinSetting.BLACKLIST) {
                    if (BotConfigHelper.isBlacklisted(botConfig, channelName)) {
                        App.logger.info("Failed to join channel: " + channelName + ". Blacklisted.");
                        return false;
                    }
                }
            }

            if (checkValidChannel && !Bot.getBot().isChannel(channelName)) {
               App.logger.info("Failed to join channel: " + channelName + ". Channel does not exist.");
               return false;

            }

            try {
                Setup.setupChannel(channelName);
            } catch (IOException e) {
                App.logger.error("Failed to setup channel: " + channelName);
                App.logger.catching(e);
                return false;
            }
            if(Bot.getBot().isConnected()) {
                if(!Bot.getBot().isConnected(channelName)) {
                    if (!Bot.getBot().join(channelName)) {
                        App.logger.warn("Failed to join channel: " + channelName);
                        return false;
                    }
                }else{
                    App.logger.error("Already in the channel: "+ channelName);
                }
            } else{
                App.logger.error("Not connected to " + ResponseParserUtil.wordCap(Configs.getGeneralConfig().getServiceName().toString(), true));
                return false;
            }
            Optional<Channel> optional = get(channelName);
            Channel channel;
            if (!optional.isPresent()) {
                ChannelConfig channelConfig = Configs.readChannelConfig(channelName);
                try {
                    channel = Channel.create(channelName, channelConfig);
                } catch (ChannelInitException e) {
                    App.logger.catching(e);
                    return false;
                }
                Bot.getBot().getChannels().put(channelName, channel);
            } else {
                channel = optional.get();
            }
            channel.join();

            if (!isBotChannel) {
                BotConfigHelper.addToCurrentChannels(botConfig, channelName);
                Configs.writeBotConfig();
            }
        } finally {
            lock.unlock();
        }
        App.logger.info("Successfully joined channel: "+channelName);
        return true;
    }

    public static boolean leave(String channelName) {
        channelName = channelName.toLowerCase();
        lock.lock();
        try {
            if (!in(channelName)) {
                App.logger.info("Not leaving channel '" + channelName + "' - not in channel");
                return false;
            } else if (channelName.equals(Bot.getBot().getUserName())) {
                App.logger.info("Not leaving channel '" + channelName + "' - cannot leave bot channel");
                return false;
            }
            App.logger.info("Leaving channel: " + channelName);
            get(channelName).ifPresent(Channel::leave);
            BotConfigHelper.removeFromCurrentChannels(Configs.getBotConfig(), channelName);
            Configs.writeBotConfig();
            Bot.getBot().leave(channelName);
        } finally {
            lock.unlock();
        }
        return true;
    }

    public static Set<String> list() {
        return Bot.getBot().getChannels().keySet();
    }
}
