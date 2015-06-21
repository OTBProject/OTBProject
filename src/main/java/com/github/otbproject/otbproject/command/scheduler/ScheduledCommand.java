package com.github.otbproject.otbproject.command.scheduler;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.user.UserLevel;

public class ScheduledCommand implements Runnable {

    private final Channel channel;
    private final PackagedMessage packagedMessage;

    public ScheduledCommand(Channel channel, String command){
        this.channel = channel;
        packagedMessage = new PackagedMessage(command, Bot.getBot().getUserName(), channel.getName(), channel.getName(), UserLevel.INTERNAL, MessagePriority.DEFAULT);
    }

    @Override
    public void run() {
        try {
            channel.receiveMessage(packagedMessage);
        } catch (NullPointerException npe) {
            App.logger.catching(npe);
        }
    }
}
