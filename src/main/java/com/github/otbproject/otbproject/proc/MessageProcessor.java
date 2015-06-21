package com.github.otbproject.otbproject.proc;

import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.user.UserLevel;

public class MessageProcessor {
    // Assumed to be thread-safe
    public static ProcessedMessage process(DatabaseWrapper db, String message, String channel, String user, UserLevel userLevel, boolean debug) {
        if (!TimeoutProcessor.doTimeouts(db, message, channel, user, userLevel)) {
            // Check for aliases and commands, and get appropriate parsed response
            ProcessedCommand processedCmd = CommandProcessor.process(db, message, channel, user, userLevel, debug);
            return new ProcessedMessage(processedCmd, false);
        }
        // If timed out, return empty string
        return new ProcessedMessage("", "", false, new String[0], true);
    }
}
