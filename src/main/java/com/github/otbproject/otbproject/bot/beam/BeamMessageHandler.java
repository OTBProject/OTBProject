package com.github.otbproject.otbproject.bot.beam;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.BotUtil;
import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.channel.*;
import com.github.otbproject.otbproject.event.ChannelMessageEvent;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.proc.TimeoutProcessor;
import com.github.otbproject.otbproject.user.UserLevel;
import com.github.otbproject.otbproject.user.UserLevels;
import com.github.otbproject.otbproject.util.ThreadUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import pro.beam.api.resource.chat.events.EventHandler;
import pro.beam.api.resource.chat.events.IncomingMessageEvent;
import pro.beam.api.resource.chat.events.data.IncomingMessageData;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class BeamMessageHandler implements EventHandler<IncomingMessageEvent> {
    private static final ExecutorService EXECUTOR_SERVICE;

    static {
        EXECUTOR_SERVICE = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                        .setNameFormat("Beam-in-%d")
                        .setUncaughtExceptionHandler(ThreadUtil.UNCAUGHT_EXCEPTION_HANDLER)
                        .build()
        );
    }

    private final String channelName;
    private final BeamChatChannel beamChatChannel;

    public BeamMessageHandler(String channel, BeamChatChannel beamChatChannel) {
        this.channelName = channel;
        this.beamChatChannel = beamChatChannel;
    }

    @Override
    public void onEvent(IncomingMessageEvent event) {
        EXECUTOR_SERVICE.execute(() -> {
            IncomingMessageData data = event.data;
            String message = (data.message.meta.me ? "/me " : "") + data.asString();
            String userNameLower = data.userName.toLowerCase();

            App.logger.info("<" + channelName + "> " + data.userName + ": " + message);
            beamChatChannel.userRoles.put(userNameLower, Collections.unmodifiableList(data.userRoles));

            BeamBot bot = (BeamBot) Control.bot();

            // Check if user is in timeout set
            if (beamChatChannel.timeoutSet.containsKey(userNameLower)) {
                // Check if user has user level mod or higher
                try {
                    if (BotUtil.isModOrHigher(channelName, userNameLower)) {
                        bot.removeTimeout(channelName, userNameLower);
                    } else {
                        // Delete message
                        beamChatChannel.beamChatConnectable.delete(data);
                        App.logger.info("Deleted message in channel <" + channelName + "> from user: " + data.userName);
                        return;
                    }
                } catch (ChannelNotFoundException e) {
                    App.logger.error("Channel '" + channelName + "' did not exist in which to check if user was mod before deleting message");
                    App.logger.catching(e);
                }
            }
            beamChatChannel.cacheMessage(data);


            // Check if message is from bot and sent by bot
            if (data.userName.equalsIgnoreCase(Control.bot().getUserName()) && (bot.sentMessageCache.containsKey(message))) {
                App.logger.debug("Ignoring message sent by bot");
                return;
            }

            Optional<ChannelProxy> optional = bot.channelManager().get(channelName);
            if (optional.isPresent()) {
                ChannelProxy channel = optional.get();
                UserLevel userLevel = UserLevels.getUserLevel(channel.getMainDatabaseWrapper(), channelName, userNameLower);
                PackagedMessage packagedMessage = new PackagedMessage(message, userNameLower, channelName, userLevel, MessagePriority.DEFAULT);
                bot.eventBus().post(new ChannelMessageEvent(channel, packagedMessage, TimeoutProcessor.doTimeouts(channel, packagedMessage)));
            } else {
                App.logger.error("Channel: " + channelName + " appears not to exist");
            }
        });
    }
}
