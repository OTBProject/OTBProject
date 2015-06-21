package com.github.otbproject.otbproject.messages.send;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.config.Configs;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

// This class is not in general thread-safe. Thread safety should be enforced by the
// class using this one
public class ChannelMessageSender {
    private static final ExecutorService EXECUTOR_SERVICE;

    private final Channel channel;
    private final PriorityBlockingQueue<MessageOut> queue = new PriorityBlockingQueue<>();
    private Future<?> future;
    private boolean active = false;

    static {
        EXECUTOR_SERVICE = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                        .setUncaughtExceptionHandler((t, e) -> {
                            App.logger.error("Thread crashed: " + t.getName());
                            App.logger.catching(e);
                        })
                        .build()
        );
    }

    public ChannelMessageSender(Channel channel) {
        this.channel = channel;
    }

    public boolean start() {
        if (active) {
            return false;
        }
        active = true;
        future = EXECUTOR_SERVICE.submit(this::run);
        return true;
    }

    public boolean stop() {
        if (!active) {
            return false;
        }
        active = false;
        future.cancel(true);
        queue.clear();
        return true;
    }

    // Queue itself is thread-safe, method is not
    public boolean send(MessageOut message) {
        if (!active) {
            return false;
        }

        MessagePriority priority = message.getPriority();
        // Defaults to no limit
        int limit = -1;

        if (priority == MessagePriority.HIGH) {
            limit = channel.getConfig().queueLimits.getHighPriorityLimit();
        } else if (priority == MessagePriority.DEFAULT) {
            limit = channel.getConfig().queueLimits.getDefaultPriorityLimit();
        } else if (priority == MessagePriority.LOW) {
            limit = channel.getConfig().queueLimits.getLowPriorityLimit();
        }

        // Yes, I am aware that this can be simplified, but it ends up being just
        //  about unreadable
        if ((limit >= 0) && queue.size() > limit) {
            return false;
        }

        return queue.add(message);
    }

    public void clearQueue() {
        queue.clear();
    }

    private void run() {
        try {
            Thread.currentThread().setName(channel.getName() + " Message Sender");
            MessageOut message;
            int sleepTime = Configs.getBotConfig().getMessageSendDelayInMilliseconds();

            while (true) {
                message = queue.take();
                Bot.getBot().sendMessage(channel.getName(), message.getMessage());
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            App.logger.info("Stopped message sender for " + channel.getName());
        }
    }
}
