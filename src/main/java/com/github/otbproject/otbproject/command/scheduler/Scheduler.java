package com.github.otbproject.otbproject.command.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Scheduler {
    private ScheduledExecutorService scheduledExecutorService;
    private volatile boolean running;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final String channel;

    public Scheduler(String channel) {
        this.channel = channel;
    }

    private ScheduledExecutorService getService() {
        return Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat(channel + "-scheduler")
                        .build()
        );
    }

    public boolean start() {
        lock.writeLock().lock();
        try {
            if (running) {
                return false;
            }
            running = true;
            scheduledExecutorService = getService();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean stop() {
        lock.writeLock().lock();
        try {
            if (!running) {
                return false;
            }
            running = false;
            scheduledExecutorService.shutdownNow();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isRunning() {
        lock.readLock().lock();
        try {
            return running;
        } finally {
            lock.readLock().unlock();
        }
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, long period, TimeUnit timeUnit) throws SchedulingException {
        lock.readLock().lock();
        try {
            if (!running) {
                throw new SchedulingException("Unable to schedule task - scheduler not running");
            }
            return scheduledExecutorService.scheduleAtFixedRate(task, delay, period, timeUnit);
        } finally {
            lock.readLock().unlock();
        }
    }
}
