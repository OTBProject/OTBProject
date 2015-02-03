package com.github.opentwitchbotteam.otbproject.proc;

import com.github.opentwitchbotteam.otbproject.App;
import com.github.opentwitchbotteam.otbproject.commands.Alias;
import com.github.opentwitchbotteam.otbproject.commands.AliasFields;
import com.github.opentwitchbotteam.otbproject.commands.Command;
import com.github.opentwitchbotteam.otbproject.commands.CommandFields;
import com.github.opentwitchbotteam.otbproject.commands.parser.CommandResponseParser;
import com.github.opentwitchbotteam.otbproject.database.DatabaseWrapper;
import com.github.opentwitchbotteam.otbproject.users.UserLevel;

import java.sql.SQLException;

public class CommandProcessor {
    public static String processCommand(DatabaseWrapper db, String message, String channel, String user, UserLevel userLevel, boolean debug) {
        String commandMsg = checkAlias(db, message, "");
        return checkCommand(db, commandMsg, channel, user, userLevel, debug);
    }

    private static String checkAlias(DatabaseWrapper db, String message, String originalAlias) {
        String[] splitMsg = message.split(" ", 2);
        String aliasName = splitMsg[0];

        if (originalAlias.equals("")) {
            originalAlias = aliasName;
        }
        // Prevent infinite alias loop
        else if (aliasName.equals(originalAlias)) {
            return message;
        }

        try {
            if (Alias.exists(db, aliasName) && ((Integer)Alias.get(db, aliasName, AliasFields.ENABLED) == 1)) {
                if (splitMsg.length == 1) {
                    return checkAlias(db, (String)Alias.get(db, aliasName, AliasFields.COMMAND), originalAlias);
                }
                return checkAlias(db, ((String)Alias.get(db, aliasName, AliasFields.COMMAND) + " " + aliasName), originalAlias);
            }
        }
        catch (SQLException e) {
            App.logger.catching(e);
        }

        // Return message if not an alias
        return message;
    }

    // Returns an empty string if script command
    private static String checkCommand(DatabaseWrapper db, String message, String channel, String user, UserLevel userLevel, boolean debug) {
        String[] splitMsg = message.split(" ", 2);
        String cmdName = splitMsg[0];

        // TODO check rate limit for command and ignore if not enough time has passed

        try {
            if (Command.exists(db, cmdName) && ((Integer)Command.get(db, cmdName, CommandFields.ENABLED) == 1) && (userLevel.getValue() >= UserLevel.valueOf((String)Command.get(db, cmdName, CommandFields.EXEC_USER_LEVEL)).getValue())) {
                String scriptPath = (String)Command.get(db, cmdName, CommandFields.SCRIPT);
                // Run script command
                if (scriptPath != null) {
                    if (splitMsg.length == 1) {
                        ScriptProcessor.process(scriptPath, db, new String[0], channel, user, userLevel);
                    }
                    else {
                        ScriptProcessor.process(scriptPath, db, splitMsg[1].split(" "), channel, user, userLevel);
                    }
                }
                // Else non-script command
                // Check if command is debug
                else if ((int)Command.get(db, cmdName, CommandFields.DEBUG) == 0 || debug) {
                    if (splitMsg.length == 1) {
                        return CommandResponseParser.parse(user, channel, (Integer)Command.get(db, cmdName, CommandFields.COUNT), new String[0], (String)Command.get(db, cmdName, CommandFields.RESPONSE));
                    }
                    return CommandResponseParser.parse(user, channel, (Integer)Command.get(db, cmdName, CommandFields.COUNT), splitMsg[1].split(" "), (String)Command.get(db, cmdName, CommandFields.RESPONSE));
                }
            }
        }
        catch (SQLException e) {
            App.logger.catching(e);
        }

        return "";
    }
}
