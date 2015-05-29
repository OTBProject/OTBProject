package com.github.otbproject.otbproject.channels;

import com.github.otbproject.otbproject.api.APIDatabase;
import com.github.otbproject.otbproject.commands.scheduler.Scheduler;
import com.github.otbproject.otbproject.config.ChannelConfig;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.database.SQLiteQuoteWrapper;
import com.github.otbproject.otbproject.filters.FilterGroups;
import com.github.otbproject.otbproject.filters.FilterManager;
import com.github.otbproject.otbproject.filters.Filters;
import com.github.otbproject.otbproject.messages.receive.ChannelMessageReceiver;
import com.github.otbproject.otbproject.messages.receive.MessageReceiveQueue;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.ChannelMessageSender;
import com.github.otbproject.otbproject.messages.send.MessageOut;
import com.github.otbproject.otbproject.messages.send.MessageSendQueue;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Channel {
    private final MessageSendQueue sendQueue = new MessageSendQueue(this);
    private final MessageReceiveQueue receiveQueue = new MessageReceiveQueue();
    private final ExpiringMap<String, Boolean> commandCooldownSet;
    private final ExpiringMap<String, Boolean> userCooldownSet;
    public final Set<String> subscriberStorage = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final String name;
    private final ChannelConfig config;
    private final DatabaseWrapper mainDb;
    private final SQLiteQuoteWrapper quoteDb;
    private ChannelMessageSender messageSender;
    private Thread messageSenderThread;
    private ChannelMessageReceiver messageReceiver;
    private Thread messageReceiverThread;
    private final Scheduler scheduler = new Scheduler();
    private final HashMap<String,ScheduledFuture> scheduledCommands = new HashMap<>();
    private final HashMap<String,ScheduledFuture> hourlyResetSchedules = new HashMap<>();
    public final FilterManager filterManager;
    private boolean inChannel;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Channel(String name, ChannelConfig config) throws ChannelInitException {
        this.name = name;
        this.config = config;
        this.inChannel = false;

        mainDb = APIDatabase.getChannelMainDatabase(name);
        if (mainDb == null) {
            throw new ChannelInitException(name, "Unable to get main database");
        }
        quoteDb = APIDatabase.getChannelQuoteDatabase(name);
        if (quoteDb == null) {
            throw new ChannelInitException(name, "Unable to get quote database");
        }

        commandCooldownSet = ExpiringMap.builder()
                .variableExpiration()
                .expirationPolicy(ExpiringMap.ExpirationPolicy.CREATED)
                .build();
        userCooldownSet = ExpiringMap.builder()
                .variableExpiration()
                .expirationPolicy(ExpiringMap.ExpirationPolicy.CREATED)
                .build();


        filterManager = new FilterManager(Filters.getAllFilters(mainDb), FilterGroups.getFilterGroupsMap(mainDb));
    }

    public boolean join() {
        lock.writeLock().lock();
        try {
            if (inChannel) {
                return false;
            }

            messageSender = new ChannelMessageSender(this, sendQueue);
            messageSenderThread = new Thread(messageSender);
            messageSenderThread.start();

            messageReceiver = new ChannelMessageReceiver(this, receiveQueue);
            messageReceiverThread = new Thread(messageReceiver);
            messageReceiverThread.start();

            scheduler.start();

            inChannel = true;

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean leave() {
        lock.writeLock().lock();
        try {
            if (!inChannel) {
                return false;
            }
            inChannel = false;

            messageSenderThread.interrupt();
            messageSenderThread = null;
            messageSender = null;
            sendQueue.clear();

            messageReceiverThread.interrupt();
            messageReceiverThread = null;
            messageReceiver = null;
            receiveQueue.clear();

            scheduler.stop();

            commandCooldownSet.clear();
            userCooldownSet.clear();
            subscriberStorage.clear();

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean sendMessage(MessageOut messageOut) {
        lock.readLock().lock();
        try {
            return inChannel && sendQueue.add(messageOut);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean receiveMessage(PackagedMessage packagedMessage) {
        lock.readLock().lock();
        try {
            return inChannel && receiveQueue.add(packagedMessage);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getName() {
        return name;
    }

    public boolean isInChannel() {
        lock.readLock().lock();
        try {
            return inChannel;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isUserCooldown(String user) {
        lock.readLock().lock();
        try {
            return userCooldownSet.containsKey(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addUserCooldown(String user, int time) {
        lock.readLock().lock();
        try {
            return inChannel && userCooldownSet.put(user, Boolean.TRUE, time, TimeUnit.SECONDS);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isCommandCooldown(String user) {
        lock.readLock().lock();
        try {
            return commandCooldownSet.containsKey(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addCommandCooldown(String user, int time) {
        lock.readLock().lock();
        try {
            return inChannel && commandCooldownSet.put(user, Boolean.TRUE, time, TimeUnit.SECONDS);
        } finally {
            lock.readLock().unlock();
        }
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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public HashMap<String, ScheduledFuture> getScheduledCommands() {
        return scheduledCommands;
    }

    public HashMap<String, ScheduledFuture> getHourlyResetSchedules() {
        return hourlyResetSchedules;
    }
}
