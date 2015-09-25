package com.github.otbproject.otbproject.command.scheduler;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.database.DatabaseWrapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Schedules {
    public static void scheduleCommandInSeconds(String channel, String command, long delay, long period, boolean hourReset) {
        scheduleCommand(channel, command, delay, period, hourReset, TimeUnit.SECONDS);
    }

    public static void scheduleCommandInMinutes(String channel, String command, long delay, long period, boolean hourReset) {
        scheduleCommand(channel, command, (TimeUnit.MINUTES.toSeconds(delay)), (TimeUnit.MINUTES.toSeconds(period)), hourReset, TimeUnit.SECONDS);
    }

    public static void scheduleCommandInHours(String channel, String command, long delay, long period) {
        scheduleCommand(channel, command, delay, period, false, TimeUnit.HOURS);
    }

    public static boolean isScheduled(String channelName, String command) {
        Optional<Channel> optional = Channels.get(channelName);
        if (!optional.isPresent()) {
            App.logger.error("Cannot check scheduled commands for channel '" + channelName + "' - channel is null.");
            return false;
        }
        return optional.get().hasCommandFuture(command);
    }

    public static boolean unScheduleCommand(String channelName, String command) {
        Optional<Channel> optional = Channels.get(channelName);
        if (!optional.isPresent()) {
            App.logger.error("Cannot unschedule command for channel '" + channelName + "' - channel is null.");
            return false;
        }
        return unScheduleCommand(optional.get(), command);
    }

    static boolean unScheduleCommand(Channel channel, String command) {
        if (removeFromDatabase(channel, command)) {
            doUnscheduleCommand(channel, command);
            App.logger.debug("Unscheduled command '" + command + "' in channel: " + channel.getName());
            return true;
        }
        return false;
    }

    static void doUnscheduleCommand(Channel channel, String command) {
        channel.removeCommandFuture(command);
        channel.removeResetFuture(command);
    }

    private static long getSecondsSinceTheHour() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        return TimeUnit.MINUTES.toSeconds(calendar.get(Calendar.MINUTE)) + calendar.get(Calendar.SECOND);
    }

    private static long getSecondsTillTheHour() {
        return TimeUnit.HOURS.toSeconds(1) - getSecondsSinceTheHour();
    }

    private static boolean scheduleCommand(String channelName, String command, long delay, long period, boolean hourReset, TimeUnit timeUnit) {
        App.logger.debug("Attempting to schedule command for channel '" + channelName + "' with period=" + period
                + ", offset=" + delay + " in " + timeUnit.name().toLowerCase() + ", and hourly reset: " + hourReset);
        Optional<Channel> optional = Channels.get(channelName);
        if (!optional.isPresent()) {
            App.logger.error("Cannot schedule command for channel '" + channelName + "' - channel is null.");
            return false;
        }
        Channel channel = optional.get();
        if (addToDatabase(channel, command, delay, period, hourReset, timeUnit)) {
            doScheduleCommand(channel, command, delay, period, hourReset, timeUnit);
            return true;
        }
        return false;
    }

    // Channel should not be null
    static void doScheduleCommand(Channel channel, String command, long delay, long period, boolean hourReset, TimeUnit timeUnit) {
        Runnable task = new ScheduledCommand(channel, command);
        if (!hourReset) {
            scheduleTask(channel, command, task, ((delay == 0) ? period : delay), period, timeUnit);
        } else {
            long result = getSecondsSinceTheHour() - delay;
            if (result < 0) {
                scheduleTask(channel, command, task, (-1 * result), period, timeUnit);
            } else {
                // This gets the correct delay. Trust me
                scheduleTask(channel, command, task, (period - (result % period)), period, timeUnit);
            }
            // Setup reset
            Runnable reset = new ResetTask(channel, command, delay, period, timeUnit);
            try {
                channel.putResetFuture(command, channel.getScheduler().schedule(reset, getSecondsTillTheHour(), TimeUnit.HOURS.toSeconds(1), TimeUnit.SECONDS));
            } catch (SchedulingException e) {
                App.logger.catching(e);
            }
        }
    }

    // Channel should not be null
    private static boolean scheduleTask(Channel channel, String command, Runnable task, long delay, long period, TimeUnit timeUnit) {
        try {
            channel.putCommandFuture(command, channel.getScheduler().schedule(task, delay, period, timeUnit));
        } catch (SchedulingException e) {
            App.logger.catching(e);
            return false;
        }
        App.logger.debug("Scheduled command '" + command + "' to run every " + period + " " + timeUnit.toString().toLowerCase() + " in channel: " + channel.getName());
        return true;
    }

    private static boolean addToDatabase(Channel channel, String command, long delay, long period, boolean hourReset, TimeUnit timeUnit) {
        DatabaseWrapper db = channel.getMainDatabaseWrapper();
        if (db.exists(SchedulerFields.TABLE_NAME, command, SchedulerFields.COMMAND)) {
            return false;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put(SchedulerFields.COMMAND, command);
        map.put(SchedulerFields.TYPE, timeUnit.toString());
        map.put(SchedulerFields.OFFSET, delay);
        map.put(SchedulerFields.PERIOD, period);
        map.put(SchedulerFields.RESET, Boolean.toString(hourReset));
        return db.insertRecord(SchedulerFields.TABLE_NAME, map);
    }

    public static void loadFromDatabase(String channelName) {
        Optional<Channel> optional = Channels.get(channelName);
        if (!optional.isPresent()) {
            return;
        }
        loadFromDatabase(optional.get());
    }

    public static void loadFromDatabase(Channel channel) {
        DatabaseWrapper db = channel.getMainDatabaseWrapper();
        db.tableDump(SchedulerFields.TABLE_NAME,
                rs -> {
                    while (rs.next()) {
                        String command = rs.getString(SchedulerFields.COMMAND);
                        long delay = rs.getLong(SchedulerFields.OFFSET);
                        long period = rs.getLong(SchedulerFields.PERIOD);
                        boolean hourReset = Boolean.parseBoolean(rs.getString(SchedulerFields.RESET));
                        TimeUnit timeUnit = TimeUnit.valueOf(rs.getString(SchedulerFields.TYPE));
                        doScheduleCommand(channel, command, delay, period, hourReset, timeUnit);
                    }
                    return null;
                });
    }

    private static boolean removeFromDatabase(Channel channel, String command) {
        DatabaseWrapper db = channel.getMainDatabaseWrapper();
        return db.removeRecord(SchedulerFields.TABLE_NAME, command, SchedulerFields.COMMAND);
    }

    public static Set<String> getScheduledCommands(String channelName) {
        Optional<Channel> optional = Channels.get(channelName);
        if (!optional.isPresent()) {
            return Collections.<String>emptySet();
        }
        return optional.get().getScheduledCommands();
    }
}
