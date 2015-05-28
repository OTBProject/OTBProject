package com.github.otbproject.otbproject.commands.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Scheduler {
    private ScheduledExecutorService scheduledExecutorService;
    private boolean running;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public boolean start() {
        lock.writeLock().lock();
        try {
            if (running) {
                return false;
            }
            running = true;
            scheduledExecutorService = Executors.newScheduledThreadPool(5);
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

    public ScheduledFuture<?> schedule(Runnable task, long delay, long period, TimeUnit timeUnit){
        lock.readLock().lock();
        try {
            return scheduledExecutorService.scheduleAtFixedRate(task, delay, period, timeUnit);
        } finally {
            lock.readLock().unlock();
        }
    }
}
