package com.github.otbproject.otbproject.command;


import com.github.otbproject.otbproject.database.DataTypes;
import com.github.otbproject.otbproject.database.TableFields;

import java.util.HashMap;
import java.util.HashSet;

public class CommandFields {
    public static final String NAME = "name";
    public static final String RESPONSE = "response";
    public static final String EXEC_USER_LEVEL = "execUserLevel";
    public static final String MIN_ARGS = "minArgs";
    public static final String COUNT = "count";
    public static final String NAME_MODIFYING_UL = "nameModifyingUL";
    public static final String RESPONSE_MODIFYING_UL = "responseModifyingUL";
    public static final String USER_LEVEL_MODIFYING_UL = "userLevelModifyingUL";
    public static final String SCRIPT = "script";
    public static final String ENABLED = "enabled";
    public static final String DEBUG = "debug"; // If true, only sends if channel set for debug

    public static final String TABLE_NAME = "tblCommands";
    public static final HashSet<String> PRIMARY_KEYS = new HashSet<>();

    static {
        PRIMARY_KEYS.add(NAME);
    }

    public static TableFields getTableFields() {
        HashMap<String,String> commandFields = new HashMap<>();
        commandFields.put(NAME, DataTypes.STRING);
        commandFields.put(RESPONSE, DataTypes.STRING);
        commandFields.put(EXEC_USER_LEVEL, DataTypes.STRING);
        commandFields.put(MIN_ARGS, DataTypes.STRING);
        commandFields.put(COUNT, DataTypes.STRING);
        commandFields.put(NAME_MODIFYING_UL, DataTypes.STRING);
        commandFields.put(RESPONSE_MODIFYING_UL, DataTypes.STRING);
        commandFields.put(USER_LEVEL_MODIFYING_UL, DataTypes.STRING);
        commandFields.put(SCRIPT, DataTypes.STRING);
        commandFields.put(ENABLED, DataTypes.STRING);
        commandFields.put(DEBUG, DataTypes.STRING);

        return new TableFields(commandFields, PRIMARY_KEYS);
    }

}
