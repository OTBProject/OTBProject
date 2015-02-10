package com.github.otbproject.otbproject.commands.loader;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.commands.Alias;
import com.github.otbproject.otbproject.commands.AliasFields;
import com.github.otbproject.otbproject.commands.Command;
import com.github.otbproject.otbproject.commands.CommandFields;
import com.github.otbproject.otbproject.database.DatabaseWrapper;

import java.sql.SQLException;
import java.util.HashMap;

public class CommandLoader {
    public static boolean addCommandFromLoadedCommand(DatabaseWrapper db, LoadedCommand loadedCommand) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put(CommandFields.NAME, loadedCommand.getName());
        map.put(CommandFields.RESPONSE, loadedCommand.getResponse());
        map.put(CommandFields.EXEC_USER_LEVEL, loadedCommand.getExecUserLevel());
        map.put(CommandFields.MIN_ARGS, loadedCommand.getMinArgs());
        map.put(CommandFields.COUNT, 0);
        map.put(CommandFields.NAME_MODIFYING_UL, loadedCommand.modifyingUserLevels.getNameModifyingUL());
        map.put(CommandFields.RESPONSE_MODIFYING_UL, loadedCommand.modifyingUserLevels.getResponseModifyingUL());
        map.put(CommandFields.USER_LEVEL_MODIFYING_UL, loadedCommand.modifyingUserLevels.getUserLevelModifyingUL());
        map.put(CommandFields.SCRIPT, loadedCommand.getScript());
        map.put(CommandFields.ENABLED, loadedCommand.isEnabled());
        map.put(CommandFields.DEBUG, loadedCommand.isDebug());

        try {
            if (Command.exists(db, loadedCommand.getName())) {
                Command.update(db, map);
            }
            else {
                Command.add(db, map);
            }
        }
        catch (SQLException e) {
            App.logger.catching(e);
            return false;
        }
        return true;
    }

    public static boolean addAliasFromLoadedAlias(DatabaseWrapper db, LoadedAlias loadedAlias) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put(AliasFields.NAME, loadedAlias.getName());
        map.put(AliasFields.COMMAND, loadedAlias.getCommand());
        map.put(AliasFields.MODIFYING_UL, loadedAlias.getModifyingUserLevel());
        map.put(AliasFields.ENABLED, loadedAlias.isEnabled());

        try {
            if (Alias.exists(db, loadedAlias.getName())) {
                Alias.update(db, map);
            } else {
                Alias.add(db, map);
            }
        }
        catch (SQLException e) {
            App.logger.catching(e);
            return false;
        }
        return true;
    }
}
