package com.github.otbproject.otbproject.channels;

import com.github.otbproject.otbproject.api.APIDatabase;
import com.github.otbproject.otbproject.commands.scheduler.Scheduler;
import com.github.otbproject.otbproject.config.ChannelConfig;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.database.SQLiteQuoteWrapper;
import com.github.otbproject.otbproject.messages.receive.ChannelMessageProcessor;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.ChannelMessageSender;
import com.github.otbproject.otbproject.messages.send.MessageOut;
import com.github.otbproject.otbproject.proc.CooldownSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Channel {
    private final CooldownSet<String> commandCooldownSet = new CooldownSet<>();
    private final CooldownSet<String> userCooldownSet = new CooldownSet<>();
    public final Set<String> subscriberStorage = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final String name;
    private final ChannelConfig config;
    private final DatabaseWrapper mainDb;
    private final SQLiteQuoteWrapper quoteDb;
    private ChannelMessageSender messageSender;
    private ChannelMessageProcessor messageProcessor;
    private final Scheduler scheduler = new Scheduler();
    private final HashMap<String,ScheduledFuture> scheduledCommands = new HashMap<>();
    private final HashMap<String,ScheduledFuture> hourlyResetSchedules = new HashMap<>();
    //public final FilterManager filterManager;
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

        //filterManager = new FilterManager(Filters.getAllFilters(mainDb), FilterGroups.getFilterGroupsMap(mainDb));
    }

    public boolean join() {
        lock.writeLock().lock();
        try {
            if (inChannel) {
                return false;
            }

            messageSender = new ChannelMessageSender(this);
            messageSender.start();

            messageProcessor = new ChannelMessageProcessor(this);

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

            messageSender.stop();
            messageSender = null;

            messageProcessor = null;

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
            return inChannel && messageSender.send(messageOut);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean receiveMessage(PackagedMessage packagedMessage) {
        if (inChannel) {
            messageProcessor.processMessage(packagedMessage);
        }
        return inChannel;
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
            return userCooldownSet.contains(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addUserCooldown(String user, int time) {
        lock.readLock().lock();
        try {
            return inChannel && userCooldownSet.add(user, time);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isCommandCooldown(String user) {
        lock.readLock().lock();
        try {
            return commandCooldownSet.contains(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addCommandCooldown(String user, int time) {
        lock.readLock().lock();
        try {
            return inChannel && commandCooldownSet.add(user, time);
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
