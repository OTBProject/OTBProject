package com.github.otbproject.otbproject.channels;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.api.APIDatabase;
import com.github.otbproject.otbproject.config.ChannelConfig;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.database.SQLiteQuoteWrapper;
import com.github.otbproject.otbproject.messages.receive.ChannelMessageReceiver;
import com.github.otbproject.otbproject.messages.receive.MessageReceiveQueue;
import com.github.otbproject.otbproject.messages.send.ChannelMessageSender;
import com.github.otbproject.otbproject.messages.send.MessageSendQueue;
import com.github.otbproject.otbproject.proc.CooldownSet;
import com.github.otbproject.otbproject.util.BlockingHashSet;

public class Channel {
    public final MessageSendQueue sendQueue = new MessageSendQueue(this);
    public final MessageReceiveQueue receiveQueue = new MessageReceiveQueue();
    public final CooldownSet commandCooldownSet = new CooldownSet();
    public final CooldownSet userCooldownSet = new CooldownSet();
    public final BlockingHashSet subscriberStorage = new BlockingHashSet();
    private final String name;
    private ChannelConfig config;
    private DatabaseWrapper mainDb;
    private SQLiteQuoteWrapper quoteDb;
    private ChannelMessageSender messageSender;
    private Thread messageSenderThread;
    private ChannelMessageReceiver messageReceiver;
    private Thread messageReceiverThread;
    private boolean inChannel;

    public Channel(String name, ChannelConfig config) {
        this.name = name;
        this.config = config;
        this.inChannel = false;
    }

    public boolean join() {
        mainDb = APIDatabase.getChannelMainDatabase(name);
        if (mainDb == null) {
            App.logger.error("Unable to get main database for channel: " + name);
            return false;
        }

        quoteDb = APIDatabase.getChannelQuoteDatabase(name);
        if (quoteDb == null) {
            App.logger.error("Unable to get quote database for channel: " + name);
            return false;
        }

        // Just in case a message got added after queue was emptied in a leave() call due to bad thread timing
        sendQueue.clear();
        receiveQueue.clear();

        messageSender = new ChannelMessageSender(this, sendQueue);
        messageSenderThread = new Thread(messageSender);
        messageSenderThread.start();

        messageReceiver = new ChannelMessageReceiver(this, receiveQueue);
        messageReceiverThread = new Thread(messageReceiver);
        messageReceiverThread.start();

        inChannel = true;

        return true;
    }

    public void leave() {
        inChannel = false;

        messageSenderThread.interrupt();
        messageSenderThread = null;
        messageSender = null;
        sendQueue.clear();

        messageReceiverThread.interrupt();
        messageReceiverThread = null;
        messageReceiver = null;
        receiveQueue.clear();

        commandCooldownSet.clear();
        userCooldownSet.clear();
        subscriberStorage.clear();

        mainDb = null;
    }

    public String getName() {
        return name;
    }

    public boolean isInChannel() {
        return inChannel;
    }

    public DatabaseWrapper getMainDatabaseWrapper() {
        return mainDb;
    }

    public SQLiteQuoteWrapper getQuoteDatabaseWrapper() {
        return quoteDb;
    }

    public ChannelConfig getConfig() {
        return config;
    }
}
