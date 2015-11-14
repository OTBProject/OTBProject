package com.github.otbproject.otbproject.command.scheduler;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.channel.ChannelProxy;
import com.github.otbproject.otbproject.database.DatabaseWrapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Schedules {
    private Schedules() {}

    public static void scheduleCommandInSeconds(String channel, String command, long delay, long period, boolean hourReset) {
        scheduleCommand(channel, command, TimeUnit.SECONDS.toMillis(delay), TimeUnit.SECONDS.toMillis(period), hourReset, TimeUnit.MILLISECONDS);
    }

    public static void scheduleCommandInMinutes(String channel, String command, long delay, long period, boolean hourReset) {
        scheduleCommand(channel, command, TimeUnit.MINUTES.toMillis(delay), TimeUnit.MINUTES.toMillis(period), hourReset, TimeUnit.MILLISECONDS);
    }

    public static void scheduleCommandInHours(String channel, String command, long delay, long period) {
        scheduleCommand(channel, command, delay, period, false, TimeUnit.HOURS);
    }

    public static boolean isScheduled(String channelName, String command) {
        Optional<ChannelProxy> optional = Control.bot().channelManager().get(channelName);
        if (!optional.isPresent()) {
            App.logger.error("Cannot check scheduled commands for channel '" + channelName + "' - channel is null.");
            return false;
        }
        return optional.get().getScheduleManager().hasCommandFuture(command);
    }

    public static boolean unScheduleCommand(String channelName, String command) {
        Optional<ChannelProxy> optional = Control.bot().channelManager().get(channelName);
        if (!optional.isPresent()) {
            App.logger.error("Cannot unschedule command for channel '" + channelName + "' - channel is null.");
            return false;
        }
        return unScheduleCommand(optional.get(), command);
    }

    static boolean unScheduleCommand(ChannelProxy channel, String command) {
        if (removeFromDatabase(channel, command)) {
            channel.getScheduleManager().removeCommandFuture(command);
            channel.getScheduleManager().removeResetFuture(command);
            App.logger.debug("Unscheduled command '" + command + "' in channel: " + channel.getName());
            return true;
        }
        return false;
    }

    private static long getMillisSinceTheHour() {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        return TimeUnit.MINUTES.toMillis(calendar.get(Calendar.MINUTE))
                + TimeUnit.SECONDS.toMillis(calendar.get(Calendar.SECOND))
                + calendar.get(Calendar.MILLISECOND);
    }

    private static boolean scheduleCommand(String channelName, String command, long delay, long period, boolean hourReset, TimeUnit timeUnit) {
        App.logger.debug("Attempting to schedule command for channel '" + channelName + "' with period=" + period
                + ", offset=" + delay + " in " + timeUnit.name().toLowerCase() + ", and hourly reset: " + hourReset);
        Optional<ChannelProxy> optional = Control.bot().channelManager().get(channelName);
        if (!optional.isPresent()) {
            App.logger.error("Cannot schedule command for channel '" + channelName + "' - channel is null.");
            return false;
        }
        ChannelProxy channel = optional.get();
        if (addToDatabase(channel, command, delay, period, hourReset, timeUnit)) {
            doScheduleCommand(channel, command, delay, period, hourReset, timeUnit);
            return true;
        }
        return false;
    }

    // Channel should not be null
    static void doScheduleCommand(ChannelProxy channel, String command, long delay, long period, boolean hourReset, TimeUnit timeUnit) {
        Runnable task = new ScheduledCommand(channel, command);
        if (!hourReset) {
            scheduleTask(channel, command, task, ((delay == 0) ? period : delay), period, timeUnit);
        } else {
            // Setup reset
            long millisSinceHour = getMillisSinceTheHour();
            long millisTillHour = TimeUnit.HOURS.toMillis(1) - millisSinceHour;
            Runnable reset = new ResetTask(channel, command, delay, period, timeUnit);
            try {
                channel.getScheduleManager().putResetFuture(command, channel.getScheduler().schedule(reset, millisTillHour, TimeUnit.HOURS.toMillis(1), TimeUnit.MILLISECONDS));
            } catch (SchedulingException e) {
                App.logger.error(e.getMessage());
                App.logger.error("Failed to schedule ResetTask for command: " + command);
                return; // Don't try to schedule command if scheduling ResetTask failed
            }

            long result = millisSinceHour - delay;
            if (result < 0) {
                scheduleTask(channel, command, task, (-1 * result), period, timeUnit);
            } else {
                // This gets the correct delay. Trust me
                scheduleTask(channel, command, task, (period - (result % period)), period, timeUnit);
            }

        }
    }

    // Channel should not be null
    private static boolean scheduleTask(ChannelProxy channel, String command, Runnable task, long delay, long period, TimeUnit timeUnit) {
        try {
            channel.getScheduleManager().putCommandFuture(command, channel.getScheduler().schedule(task, delay, period, timeUnit));
        } catch (SchedulingException e) {
            App.logger.catching(e);
            return false;
        }
        App.logger.debug("Scheduled command '" + command + "' to run every " + period + " " + timeUnit.toString().toLowerCase() + " in channel: " + channel.getName());
        return true;
    }

    private static boolean addToDatabase(ChannelProxy channel, String command, long delay, long period, boolean hourReset, TimeUnit timeUnit) {
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
        Optional<ChannelProxy> optional = Control.bot().channelManager().get(channelName);
        if (!optional.isPresent()) {
            return;
        }
        loadFromDatabase(optional.get());
    }

    public static void loadFromDatabase(ChannelProxy channel) {
        DatabaseWrapper db = channel.getMainDatabaseWrapper();
        db.tableDump(SchedulerFields.TABLE_NAME,
                rs -> {
                    while (rs.next()) {
                        String command = rs.getString(SchedulerFields.COMMAND);
                        long delay = rs.getLong(SchedulerFields.OFFSET);
                        long period = rs.getLong(SchedulerFields.PERIOD);
                        boolean hourReset = Boolean.parseBoolean(rs.getString(SchedulerFields.RESET));
                        TimeUnit timeUnit = TimeUnit.valueOf(rs.getString(SchedulerFields.TYPE));
                        if (hourReset) {
                            delay = timeUnit.toMillis(delay);
                            period = timeUnit.toMillis(period);
                            timeUnit = TimeUnit.MILLISECONDS;
                        }
                        doScheduleCommand(channel, command, delay, period, hourReset, timeUnit);
                    }
                    return null;
                });
    }

    private static boolean removeFromDatabase(ChannelProxy channel, String command) {
        DatabaseWrapper db = channel.getMainDatabaseWrapper();
        return db.removeRecord(SchedulerFields.TABLE_NAME, command, SchedulerFields.COMMAND);
    }

    public static Set<String> getScheduledCommands(String channelName) {
        Optional<ChannelProxy> optional = Control.bot().channelManager().get(channelName);
        if (!optional.isPresent()) {
            return Collections.<String>emptySet();
        }
        return optional.get().getScheduledCommands();
    }
}
