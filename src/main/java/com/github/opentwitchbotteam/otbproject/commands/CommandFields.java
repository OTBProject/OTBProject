package com.github.opentwitchbotteam.otbproject.commands;


import java.util.HashMap;

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

    public static HashMap<String, String> getTableHashMap()
    {
        HashMap<String, String> commandFields = new HashMap<String, String>();
        commandFields.put(NAME, "Text");
        commandFields.put(RESPONSE, "Text");
        commandFields.put(EXEC_USER_LEVEL, "Text");
        commandFields.put(MIN_ARGS, "Integer");
        commandFields.put(COUNT, "Integer");
        commandFields.put(NAME_MODIFYING_UL, "Text");
        commandFields.put(RESPONSE_MODIFYING_UL, "Text");
        commandFields.put(USER_LEVEL_MODIFYING_UL, "Text");
        commandFields.put(SCRIPT, "Text");
        commandFields.put(ENABLED, "Integer");
        commandFields.put(DEBUG, "Integer");

        return commandFields;
    }
}
