package com.github.opentwitchbotteam.otbproject.proc;

import com.github.opentwitchbotteam.otbproject.database.DatabaseWrapper;
import com.github.opentwitchbotteam.otbproject.users.UserLevel;

public class MessageProcessor {
    // Returns the response to the message (does not send messages itself)
    // Returns empty string if no response
    public static String process(DatabaseWrapper db, String message, String channel, String user, boolean subscriber, boolean debug) {
        // TODO find out if user is mod
        UserLevel userLevel = Util.getUserLevel(db, channel, user, subscriber);
        return process(db, message, channel, user, userLevel, debug);
    }

    public static String process(DatabaseWrapper db, String message, String channel, String user, UserLevel userLevel, boolean debug) {
        // TODO possibly return if timeout occurred for !at command
        if (!TimeoutProcessor.doTimeouts(db, message, channel, user, userLevel)) {
            // TODO check user rate limit and ignore if not enough time has passed
            // Check for aliases and commands, and get appropriate parsed response
            return CommandProcessor.processCommand(db, message, channel, user, userLevel, debug);
        }
        // If timed out, return empty string
        return "";
    }
}
