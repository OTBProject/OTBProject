package com.github.otbproject.otbproject.proc;

import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.channel.ChannelProxy;
import com.github.otbproject.otbproject.filter.FilterAction;
import com.github.otbproject.otbproject.filter.FilterGroup;
import com.github.otbproject.otbproject.filter.FilterProcessor;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.user.UserLevel;

import java.util.Optional;

public class TimeoutProcessor {
    private TimeoutProcessor() {}

    public static boolean doTimeouts(ChannelProxy channel, PackagedMessage packagedMessage) {
        // TODO implement and remove if statement
        if (false) { // So I can work on an implementation without changing behaviour
            Optional<FilterGroup> optional = FilterProcessor.process(channel.getFilterMap(), packagedMessage.getMessage(), packagedMessage.getUserLevel());
            if (optional.isPresent()) {
                FilterGroup filterGroup = optional.get();
                performFilterAction(packagedMessage, filterGroup.getAction());
                sendFilterMessage(channel, packagedMessage, filterGroup);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private static void performFilterAction(PackagedMessage packagedMessage, FilterAction action) {
        switch (action) {
            case BAN:
                Control.bot().ban(packagedMessage.getChannel(), packagedMessage.getUser());
                break;
            case TIMEOUT:
                Control.bot().timeout(packagedMessage.getChannel(), packagedMessage.getUser(), 600); // TODO get actual time from somewhere (config?)
                break;
            case STRIKE:
                // TODO handle strike number
                break;
            case PURGE:
                Control.bot().timeout(packagedMessage.getChannel(), packagedMessage.getUser(), 1);
                break;
            default:
                // No action to perform by default
        }
    }

    private static void sendFilterMessage(ChannelProxy channel, PackagedMessage incomingMessage, FilterGroup filterGroup) {
        // TODO handle message for timeout
        String responseCommand = "";
        switch (filterGroup.getAction()) {
            case BAN:
            case TIMEOUT:
            case PURGE:
            case WARN:
                responseCommand = filterGroup.getResponseCommand();
                break;
            case STRIKE:
                // TODO handle strike number
                break;
        }
        PackagedMessage responseMessage = new PackagedMessage(responseCommand, incomingMessage.getUser(), incomingMessage.getChannel(), incomingMessage.getDestinationChannel(), UserLevel.INTERNAL, MessagePriority.LOW);
        channel.receiveMessage(responseMessage);
    }
}
