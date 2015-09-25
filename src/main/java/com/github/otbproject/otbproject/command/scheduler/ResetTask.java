package com.github.otbproject.otbproject.command.scheduler;

import com.github.otbproject.otbproject.channel.Channel;

import java.util.concurrent.TimeUnit;

public class ResetTask implements Runnable {

    private final Channel channel;
    private final String command;
    private final long delay;
    private final long period;
    private final TimeUnit timeUnit;

    public ResetTask(Channel channel, String command, long delay, long period, TimeUnit timeUnit) {
        this.channel = channel;
        this.command = command;
        this.delay = delay;
        this.period = period;
        this.timeUnit = timeUnit;
    }

    @Override
    public void run() {
        Schedules.doUnscheduleCommand(channel, command);
        Schedules.doScheduleCommand(channel, command, delay, period, true, timeUnit);
    }
}
