package com.github.otbproject.otbproject.messages.receive;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.api.APIBot;
import com.github.otbproject.otbproject.api.APIChannel;
import com.github.otbproject.otbproject.api.APIConfig;
import com.github.otbproject.otbproject.channels.Channel;
import com.github.otbproject.otbproject.commands.Command;
import com.github.otbproject.otbproject.config.ChannelConfigHelper;
import com.github.otbproject.otbproject.config.GeneralConfigHelper;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.messages.internal.InternalMessageSender;
import com.github.otbproject.otbproject.messages.send.MessageOut;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.proc.MessageProcessor;
import com.github.otbproject.otbproject.proc.ProcessedMessage;
import com.github.otbproject.otbproject.proc.ScriptProcessor;
import com.github.otbproject.otbproject.users.UserLevel;

import java.sql.SQLException;

public class ChannelMessageReceiver implements Runnable {
    private final Channel channel;
    private MessageReceiveQueue queue;
    private boolean inBotChannel;

    public ChannelMessageReceiver(Channel channel, MessageReceiveQueue queue) {
        this.channel = channel;
        this.queue = queue;
        inBotChannel = this.channel.getName().equals(APIBot.getBot().getUserName());
    }

    public void run() {
        try {
            Thread.currentThread().setName(channel.getName() + " Message Receiver");
            PackagedMessage packagedMessage;
            boolean internal;
            String channelName = channel.getName();

            while (true) {
                packagedMessage = queue.take();
                String user = packagedMessage.getUser();

                String destChannelName = packagedMessage.getDestinationChannel();
                Channel destChannel = null;
                if (packagedMessage.getDestinationChannel().startsWith(InternalMessageSender.DESTINATION_PREFIX)) {
                    internal = true;
                } else {
                    internal = false;
                    destChannel = APIChannel.get(packagedMessage.getDestinationChannel());
                    if (destChannel == null || !APIChannel.in(destChannelName)) {
                        App.logger.warn("Attempted to process message to be sent in channel in which bot is not listening: " + destChannelName);
                        continue;
                    }
                }

                // Process commands for bot channel
                if (inBotChannel) {
                    DatabaseWrapper db = APIBot.getBot().getBotDB();
                    UserLevel ul = packagedMessage.getUserLevel();
                    ProcessedMessage processedMsg = MessageProcessor.process(db, packagedMessage.getMessage(), channelName, user, ul, true);
                    if (processedMsg.isScript() || !processedMsg.getResponse().isEmpty()) {
                        doResponse(db, processedMsg, channelName, destChannelName, destChannel, user, ul, packagedMessage.getMessagePriority(), internal);
                        // Don't process response as regular channel if done as bot channel
                        continue;
                    }
                }

                // Pre-check if user is on cooldown (skip if internal)
                if (!internal && destChannel.userCooldownSet.contains(user)) {
                    continue;
                }

                // Process commands not as bot channel
                DatabaseWrapper db = channel.getMainDatabaseWrapper();
                UserLevel ul = packagedMessage.getUserLevel();
                ProcessedMessage processedMsg = MessageProcessor.process(db, packagedMessage.getMessage(), channelName, user, ul, channel.getConfig().isDebug());

                // Check if bot is enabled
                if (channel.getConfig().isEnabled() || GeneralConfigHelper.isPermanentlyEnabled(APIConfig.getGeneralConfig(), processedMsg.getCommandName())) {
                    // Check if empty message, and then if command is on cooldown (skip cooldown check if internal)
                    if ((processedMsg.isScript() || !processedMsg.getResponse().isEmpty()) && (internal || !destChannel.commandCooldownSet.contains(processedMsg.getCommandName()))) {
                        doResponse(db, processedMsg, channelName, destChannelName, destChannel, user, ul, packagedMessage.getMessagePriority(), internal);
                    }
                }
            }
        } catch (InterruptedException e) {
            App.logger.info("Stopped message receiver for " + channel.getName());
        } catch (Exception e) {
            App.logger.catching(e);
        }
    }

    private void doResponse(DatabaseWrapper db, ProcessedMessage processedMsg, String channelName, String destinationChannelName, Channel destinationChannel, String user, UserLevel ul, MessagePriority priority, boolean internal) {
        String message = processedMsg.getResponse();
        String command = processedMsg.getCommandName();

        // Do script (processedMsg.getResponse is the script path)
        if (processedMsg.isScript()) {
            boolean success = ScriptProcessor.process(message, db, command, processedMsg.getArgs(), channelName, destinationChannelName, user, ul);
            if (!success) {
                return;
            }
        }
        // Send message
        else {
            MessageOut messageOut = new MessageOut(message, priority);
            if (internal) {
                new InternalMessageSender(destinationChannelName.replace(InternalMessageSender.DESTINATION_PREFIX, ""), messageOut.getMessage()).sendMessage();
            }
            // If queue rejects message because it's too full, return
            else if (!APIChannel.in(destinationChannelName) || !destinationChannel.sendQueue.add(messageOut)) {
                return;
            }
        }

        // Increment count
        try {
            Command.incrementCount(db, command);
        } catch (SQLException e) {
            App.logger.error("Failed to increment count for command: " + command);
            App.logger.catching(e);
        }

        // Skip cooldowns if bot channel or internal
        if (inBotChannel || internal) {
            return;
        }

        // Handles command cooldowns
        int commandCooldown = channel.getConfig().getCommandCooldown();
        if (commandCooldown > 0) {
            destinationChannel.commandCooldownSet.add(command, commandCooldown);
        }
        // Handles user cooldowns
        int userCooldown = ChannelConfigHelper.getCooldown(channel.getConfig(), ul);
        if (userCooldown > 0) {
            destinationChannel.userCooldownSet.add(user, userCooldown);
        }
    }
}
