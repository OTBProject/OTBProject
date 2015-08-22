package com.github.otbproject.otbproject.bot.irc;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.proc.TimeoutProcessor;
import com.github.otbproject.otbproject.user.UserLevel;
import com.github.otbproject.otbproject.user.UserLevels;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.util.Optional;

class IrcListener extends ListenerAdapter<PircBotX> {

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        String channelName = IRCHelper.getInternalChannelName(event.getChannel().getName());
        Optional<Channel> optional = Channels.get(channelName);
        if (!optional.isPresent()) {
            App.logger.error("The channel '" + channelName + "' really shouldn't be null here. Something has gone terribly wrong.");
            return;
        }
        Channel channel = optional.get();

        String user = event.getUser().getNick().toLowerCase();

        String message = event.getMessage();
        TwitchBot bot = (TwitchBot) Control.getBot();
        if (user.equalsIgnoreCase("jtv")) {
            if (message.contains(":SPECIALUSER")) {
                String[] messageSplit = message.split(":SPECIALUSER")[1].split(" ");
                String name = messageSplit[0];
                String userType = messageSplit[1];
                if (userType.equalsIgnoreCase("subscriber")) {
                    bot.subscriberStorage.put(channelName, name);
                }
            }
        } else {
            UserLevel userLevel = UserLevels.getUserLevel(channel.getMainDatabaseWrapper(), channelName, user);
            PackagedMessage packagedMessage = new PackagedMessage(message, user, channelName, userLevel, MessagePriority.DEFAULT);
            bot.invokeMessageHandlers(channel, packagedMessage, TimeoutProcessor.doTimeouts(channel, packagedMessage));
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
        ((TwitchBot) Control.getBot()).ircBot.sendRaw().rawLine("TWITCHCLIENT 3");
        // Join bot channel
        Channels.join(Control.getBot().getUserName(), false);
        // Join channels
        Configs.getBotConfig().getCurrentChannels().forEach(channel -> Channels.join(channel, false));
    }

}
