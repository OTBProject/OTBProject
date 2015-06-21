package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.ChannelNotFoundException;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.messages.internal.InternalMessageSender;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessageOut;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.user.UserLevel;

import java.util.Optional;
import java.util.function.Supplier;

public class ScriptHelper {
    public static void runCommand(String message, String user, String channelName, String destinationChannel, MessagePriority priority) {
        App.logger.debug("Processing message as command: " + message);
        PackagedMessage packagedMessage = new PackagedMessage(message, user, channelName, destinationChannel, UserLevel.INTERNAL, priority);
        Channels.getOrThrow(channelName).receiveMessage(packagedMessage);
    }

    public static void sendMessage(String channelName, String message, MessagePriority priority) {
        if (channelName.startsWith(InternalMessageSender.DESTINATION_PREFIX)) {
            InternalMessageSender.send(channelName.replace(InternalMessageSender.DESTINATION_PREFIX, ""), message, "CmdExec");
        } else {
            MessageOut messageOut = new MessageOut(message, priority);
            Channels.getOrThrow(channelName).sendMessage(messageOut);
        }
    }
}
