package com.github.otbproject.otbproject.proc;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.command.Alias;
import com.github.otbproject.otbproject.command.Aliases;
import com.github.otbproject.otbproject.command.Command;
import com.github.otbproject.otbproject.command.Commands;
import com.github.otbproject.otbproject.command.parser.CommandResponseParser;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.user.UserLevel;

import java.util.*;

public class CommandProcessor {
    public static ProcessedCommand process(DatabaseWrapper db, String message, String channel, String user, UserLevel userLevel, boolean debug) {
        message = message.trim();
        String commandMsg = checkAlias(db, message);
        return checkCommand(db, commandMsg, channel, user, userLevel, debug);
    }

    public static String checkAlias(DatabaseWrapper db, String message) {
        return checkAlias(db, message, new HashSet<>());
    }

    private static String checkAlias(DatabaseWrapper db, String message, HashSet<String> usedAliases) {
        String[] splitMsg = message.split(" ", 2);
        String aliasName = splitMsg[0];

        // Prevent infinite alias loop
        if (usedAliases.contains(aliasName)) {
            return message;
        } else {
            usedAliases.add(aliasName);
        }
        Alias alias = Aliases.get(db, aliasName);
        if ((alias != null) && alias.isEnabled()) {
            if (splitMsg.length == 1) {
                return checkAlias(db, alias.getCommand(), usedAliases);
            }
            return checkAlias(db, (alias.getCommand() + " " + splitMsg[1]), usedAliases);
        }
        // Return message if not an alias
        return message;
    }

    // Returns an empty string if script command
    private static ProcessedCommand checkCommand(DatabaseWrapper db, String message, String channel, String user, UserLevel userLevel, boolean debug) {
        String[] splitMsg = message.split(" ", 2);
        String cmdName = splitMsg[0];

        String[] args;
        if ((splitMsg.length == 1) || splitMsg[1].equals("")) {
            args = new String[0];
        } else {
            args = splitMsg[1].split(" ");
            List<String> tempArrayList = new ArrayList<>(Arrays.asList(args));
            for (Iterator<String> i = tempArrayList.iterator(); i.hasNext();) {
                if (i.next().isEmpty()) {
                    i.remove();
                }
            }

            if (tempArrayList.size() == 0) {
                args = new String[0];
            } else {
                args = tempArrayList.toArray(new String[tempArrayList.size()]);
            }
        }

        Command command = Commands.get(db, cmdName);
        if ((command != null) && command.isEnabled() && userLevel.getValue() >= command.getExecUserLevel().getValue() && args.length >= command.getMinArgs()) {
            App.logger.debug("Processing command: " + cmdName);
            String scriptPath = command.getScript();
            // Return script path
            if ((scriptPath != null) && !scriptPath.equals("null")) {
                return new ProcessedCommand(scriptPath, cmdName, true, args);
            }
            // Else non-script command
            // Check if command is debug
            else if (!command.isDebug() || debug) {
                String response = CommandResponseParser.parse(user, channel, (command.getCount() + 1), args, command.getResponse());
                return new ProcessedCommand(response, cmdName, false, args);
            }
        }
        return new ProcessedCommand("", "", false, args);
    }
}
