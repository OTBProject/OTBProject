package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.channel.ChannelNotFoundException;
import com.github.otbproject.otbproject.channel.ChannelProxy;
import com.github.otbproject.otbproject.messages.internal.InternalMessageSender;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessageOut;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.user.UserLevel;

public class ScriptHelper {
    private ScriptHelper() {}

    public static void runCommand(String message, String user, String channelName, String destinationChannel, MessagePriority priority) throws ChannelNotFoundException {
        App.logger.debug("Processing message as command: " + message);
        PackagedMessage packagedMessage = new PackagedMessage(message, user, channelName, destinationChannel, UserLevel.INTERNAL, priority);
        Control.bot().channelManager().getOrThrow(channelName).receiveMessage(packagedMessage);
    }

    public static void sendMessage(String channelName, String message, MessagePriority priority) throws ChannelNotFoundException {
        if (channelName.startsWith(InternalMessageSender.DESTINATION_PREFIX)) {
            InternalMessageSender.send(channelName.substring(InternalMessageSender.DESTINATION_PREFIX.length()), message, "CmdExec");
        } else {
            MessageOut messageOut = new MessageOut(message, priority);
            Control.bot().channelManager().getOrThrow(channelName).sendMessage(messageOut);
        }
    }

    public static void sendMessage(ChannelProxy channelProxy, String message, MessagePriority priority) throws ChannelNotFoundException {
        channelProxy.sendMessage(new MessageOut(message, priority));
    }
}
