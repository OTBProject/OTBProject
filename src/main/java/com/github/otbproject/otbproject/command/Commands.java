package com.github.otbproject.otbproject.command;


import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.user.UserLevel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Commands {

    public static Command get(DatabaseWrapper db, String commandName) {
        if (db.exists(CommandFields.TABLE_NAME, commandName, CommandFields.NAME)) {
            Command command = new Command();
            ResultSet rs = db.getRecord(CommandFields.TABLE_NAME, commandName, CommandFields.NAME);
            try {
                command.setName(rs.getString(CommandFields.NAME));
                command.setResponse(rs.getString(CommandFields.RESPONSE));
                command.setCount(Integer.parseInt(rs.getString(CommandFields.COUNT)));
                command.setEnabled(Boolean.valueOf(rs.getString(CommandFields.ENABLED)));
                command.setDebug((Boolean.valueOf(rs.getString(CommandFields.DEBUG))));
                command.setExecUserLevel(UserLevel.valueOf(rs.getString(CommandFields.EXEC_USER_LEVEL)));
                command.setMinArgs(Integer.parseInt(rs.getString(CommandFields.MIN_ARGS)));
                command.setScript(rs.getString(CommandFields.SCRIPT));
                command.modifyingUserLevels = command.new ModifyingUserLevels();
                command.modifyingUserLevels.setNameModifyingUL(UserLevel.valueOf(rs.getString(CommandFields.NAME_MODIFYING_UL)));
                command.modifyingUserLevels.setResponseModifyingUL(UserLevel.valueOf(rs.getString(CommandFields.RESPONSE_MODIFYING_UL)));
                command.modifyingUserLevels.setUserLevelModifyingUL(UserLevel.valueOf(rs.getString(CommandFields.USER_LEVEL_MODIFYING_UL)));
                return command;
            } catch (SQLException e) {
                App.logger.catching(e);
            } finally {
                try {
                    rs.close();
                } catch (SQLException e) {
                    App.logger.catching(e);
                }
            }
        }
        return null;
    }

    public static List<String> getCommands(DatabaseWrapper db) {
        ArrayList<Object> list =  db.getRecordsList(CommandFields.TABLE_NAME, CommandFields.NAME);
        if (list == null) {
            return null;
        }
        try {
            return list.stream().map(key -> (String) key).collect(Collectors.toList());
        } catch (ClassCastException e) {
            App.logger.catching(e);
            return null;
        }
    }

    public static boolean update(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.updateRecord(CommandFields.TABLE_NAME, map.get(CommandFields.NAME), CommandFields.NAME, map);
    }

    public static boolean exists(DatabaseWrapper db, String commandName) {
        return db.exists(CommandFields.TABLE_NAME, commandName, CommandFields.NAME);
    }

    public static boolean add(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.insertRecord(CommandFields.TABLE_NAME, map);
    }

    public static boolean remove(DatabaseWrapper db, String commandName) {
        return db.removeRecord(CommandFields.TABLE_NAME, commandName, CommandFields.NAME);
    }

    public static void incrementCount(DatabaseWrapper db, String commandName) {
        Command command = get(db, commandName);
        if (command == null) {
            return;
        }
        command.setCount(command.getCount() + 1);
        addCommandFromLoadedCommand(db, command);
    }

    public static void resetCount(DatabaseWrapper db, String commandName) {
        Command command = get(db, commandName);
        if (command == null) {
            return;
        }
        command.setCount(0);
        addCommandFromLoadedCommand(db, command);
    }

    public static boolean addCommandFromLoadedCommand(DatabaseWrapper db, Command command) {
        HashMap<String, Object> map = new HashMap<>();

        map.put(CommandFields.NAME, command.getName());
        map.put(CommandFields.RESPONSE, command.getResponse());
        map.put(CommandFields.EXEC_USER_LEVEL, command.getExecUserLevel().name());
        map.put(CommandFields.MIN_ARGS, String.valueOf(command.getMinArgs()));
        map.put(CommandFields.COUNT, String.valueOf(command.getCount()));
        map.put(CommandFields.NAME_MODIFYING_UL, command.modifyingUserLevels.getNameModifyingUL().name());
        map.put(CommandFields.RESPONSE_MODIFYING_UL, command.modifyingUserLevels.getResponseModifyingUL().name());
        map.put(CommandFields.USER_LEVEL_MODIFYING_UL, command.modifyingUserLevels.getUserLevelModifyingUL().name());
        map.put(CommandFields.SCRIPT, command.getScript());
        map.put(CommandFields.ENABLED, String.valueOf(command.isEnabled()));
        map.put(CommandFields.DEBUG, String.valueOf(command.isDebug()));

        if (Commands.exists(db, command.getName())) {
            return Commands.update(db, map);
        } else {
            return Commands.add(db, map);
        }
    }
}
