package com.github.otbproject.otbproject.bot.beam;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.bot.BotUtil;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.ChannelNotFoundException;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.user.UserLevel;
import com.github.otbproject.otbproject.user.UserLevels;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import pro.beam.api.resource.chat.events.EventHandler;
import pro.beam.api.resource.chat.events.IncomingMessageEvent;
import pro.beam.api.resource.chat.events.data.IncomingMessageData;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler implements EventHandler<IncomingMessageEvent> {
    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        EXECUTOR_SERVICE = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                        .setNameFormat("Beam-in-%d")
                        .setUncaughtExceptionHandler((t, e) -> {
                            App.logger.error("Thread crashed: " + t.getName());
                            App.logger.catching(e);
                        })
                        .build()
        );
    }

    private final String channelName;
    private final BeamChatChannel beamChatChannel;
    public MessageHandler(String channel, BeamChatChannel beamChatChannel){
        this.channelName = channel;
        this.beamChatChannel = beamChatChannel;
    }

    @Override
    public void onEvent(IncomingMessageEvent event) {
        EXECUTOR_SERVICE.execute(() -> {
            IncomingMessageData data = event.data;
            App.logger.info("<"+ channelName +"> "+ data.user_name + ": " + getMessage(data));
            beamChatChannel.userRoles.put(data.user_name.toLowerCase(), Collections.unmodifiableList(data.user_roles));

            BeamBot bot = (BeamBot) Bot.getBot();

            // Check if user is in timeout set
            if (beamChatChannel.timeoutSet.containsKey(data.user_name.toLowerCase())) {
                // Check if user has user level mod or higher
                try {
                    if (BotUtil.isModOrHigher(channelName, data.user_name.toLowerCase())) {
                        bot.removeTimeout(channelName, data.user_name.toLowerCase());
                    } else {
                        // Delete message
                        beamChatChannel.beamChatConnectable.delete(data);
                        App.logger.info("Deleted message in channel <" + channelName + "> from user: " + data.user_name);
                        return;
                    }
                } catch (ChannelNotFoundException e) {
                    App.logger.error("Channel '" + channelName + "' did not exist in which to check if user was mod before deleting message");
                    App.logger.catching(e);
                }
            }
            beamChatChannel.cacheMessage(data);

            // Check if message is from bot and sent by bot
            if (data.user_name.equalsIgnoreCase(Bot.getBot().getUserName()) && (bot.sentMessageCache.containsKey(data.getMessage()))) {
                App.logger.debug("Ignoring message sent by bot");
                return;
            }

            Optional<Channel> optional = Channels.get(channelName);
            if(optional.isPresent()){
                Channel channel = optional.get();
                PackagedMessage packagedMessage = new PackagedMessage(getMessage(data),data.user_name.toLowerCase(), channelName, UserLevels.getUserLevel(channel.getMainDatabaseWrapper(), channelName, data.user_name.toLowerCase()), MessagePriority.DEFAULT);
                channel.receiveMessage(packagedMessage);
            } else {
                App.logger.error("Channel: " + channelName + " appears not to exist");
            }
        });
    }

    // Modified version of Beam's default method to join message parts
    // Does not insert a space between different parts like Beam's method does, and gets
    //  plaintext of emoticons
    private static String getMessage(IncomingMessageData data) {
        return Joiner.on("").join(Iterators.transform(data.message.iterator(), part -> {
            switch(part.type) {
                case ME:
                    return "/me " + part.text;
                case EMOTICON:
                    return part.text;
                case LINK:
                    return part.url;
                case TEXT:
                default:
                    return part.data;
            }
        }));
    }
}
