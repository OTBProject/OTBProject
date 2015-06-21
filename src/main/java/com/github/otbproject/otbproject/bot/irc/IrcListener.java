package com.github.otbproject.otbproject.bot.irc;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.user.UserLevel;
import com.github.otbproject.otbproject.user.UserLevels;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.util.Optional;

public class IrcListener extends ListenerAdapter {

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        String channelName = IRCBot.getInternalChannelName(event.getChannel().getName());
        Optional<Channel> optional = Channels.get(channelName);
        if (!optional.isPresent()) {
            App.logger.error("The channel '" + channelName + "' really shouldn't be null here. Something has gone terribly wrong.");
            return;
        }
        Channel channel = optional.get();

        String user = event.getUser().getNick();

        String message = event.getMessage();
        if (user.equalsIgnoreCase("jtv")) {
            if (message.contains(":SPECIALUSER")) {
                String[] messageSplit = message.split(":SPECIALUSER")[1].split(" ");
                String name = messageSplit[0];
                String userType = messageSplit[1];
                if (userType.equalsIgnoreCase("subscriber")) {
                    channel.subscriberStorage.add(name);
                }
            }
        } else {
            UserLevel userLevel = UserLevels.getUserLevel(channel.getMainDatabaseWrapper(), channelName, user);
            channel.receiveMessage(new PackagedMessage(message, user, channelName, userLevel, MessagePriority.DEFAULT));
        }

    }

    @Override
    public void onJoin(JoinEvent event) {
    }

    @Override
    public void onPart(PartEvent event) {
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        App.logger.info("Disconnected From Twitch");
    }

    @Override
    public void onConnect(ConnectEvent event) {
        ((IRCBot) Bot.getBot()).sendRaw().rawLine("TWITCHCLIENT 3");
        // Join bot channel
        Channels.join(Bot.getBot().getUserName(), false);
        // Join channels
        Configs.getBotConfig().currentChannels.forEach(channel -> Channels.join(channel, false));
    }

}
