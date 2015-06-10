package com.github.otbproject.otbproject.channels;

import com.github.otbproject.otbproject.api.Databases;
import com.github.otbproject.otbproject.api.APISchedule;
import com.github.otbproject.otbproject.commands.scheduler.Scheduler;
import com.github.otbproject.otbproject.config.ChannelConfig;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.database.SQLiteQuoteWrapper;
import com.github.otbproject.otbproject.filters.GroupFilterSet;
import com.github.otbproject.otbproject.messages.receive.ChannelMessageProcessor;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.ChannelMessageSender;
import com.github.otbproject.otbproject.messages.send.MessageOut;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Channel {
    private final ExpiringMap<String, Boolean> commandCooldownSet;
    private final ExpiringMap<String, Boolean> userCooldownSet;
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
    private ConcurrentMap<String, GroupFilterSet> filterMap;
    private boolean inChannel;

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    private Channel(String name, ChannelConfig config) throws ChannelInitException {
        this.name = name;
        this.config = config;
        this.inChannel = false;

        mainDb = Databases.getChannelMainDatabase(name);
        if (mainDb == null) {
            throw new ChannelInitException(name, "Unable to get main database");
        }
        quoteDb = Databases.getChannelQuoteDatabase(name);
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

        //filterMap = GroupFilterSet.createGroupFilterSetMap(FilterGroups.getFilterGroups(mainDb), Filters.getAllFilters(mainDb));
    }

    private void init() {
        messageSender = new ChannelMessageSender(this);
        messageProcessor = new ChannelMessageProcessor(this);
        APISchedule.loadFromDatabase(this);
    }

    public static Channel create(String name, ChannelConfig config) throws ChannelInitException {
        Channel channel = new Channel(name, config);
        channel.init();
        return channel;
    }

    public boolean join() {
        lock.writeLock().lock();
        try {
            if (inChannel) {
                return false;
            }

            messageSender.start();
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
            return inChannel && !config.isSilenced() && messageSender.send(messageOut);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clearSendQueue() {
        lock.readLock().lock();
        try {
            messageSender.clearQueue();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Not concurrent.
     * Cannot read-lock because:
     *  * It may execute a script and may consequently take an extended time to execute
     *  * It may execute a script which requires a write-lock (such as one to leave
     *      the channel), which will cause it to lock up.
     *
     * Checks if in the channel only initially, and then attempts to process the
     *  message. Some calls from within messageProcessor.process() may fail if
     *  the bot leaves the channel while it is still executing.
     *
     * @param packagedMessage a message to receive and process
     * @return A boolean stating whether it is likely that the message was processed
     *  successfully. Should not be relied upon to be accurate
     */
    public boolean receiveMessage(PackagedMessage packagedMessage) {
        if (inChannel) {
            messageProcessor.process(packagedMessage);
        } else {
            return false; // In case joins channel since the 'if' statement
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
            return userCooldownSet.containsKey(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addUserCooldown(String user, int time) {
        lock.readLock().lock();
        try {
            if (inChannel) {
                userCooldownSet.put(user, Boolean.TRUE, time, TimeUnit.SECONDS);
                return true;
            }
            return false;
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
            if (inChannel) {
                commandCooldownSet.put(user, Boolean.TRUE, time, TimeUnit.SECONDS);
                return true;
            }
            return false;
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

    public ConcurrentMap<String, GroupFilterSet> getFilterMap() {
        return filterMap;
    }

    public void setFilterMap(ConcurrentMap<String, GroupFilterSet> filterMap) {
        this.filterMap = filterMap;
    }
}
