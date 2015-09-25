package com.github.otbproject.otbproject.command.scheduler;

import com.github.otbproject.otbproject.database.DataTypes;
import com.github.otbproject.otbproject.database.TableFields;

import java.util.HashMap;
import java.util.HashSet;

public class SchedulerFields {
    public static final String COMMAND = "command";
    public static final String TYPE = "type";
    public static final String OFFSET = "offset";
    public static final String PERIOD = "period";
    public static final String RESET = "reset";

    public static final String TABLE_NAME = "tblSchedule";
    static final HashSet<String> PRIMARY_KEYS = new HashSet<>();

    static {
        PRIMARY_KEYS.add(COMMAND);
    }

    public static TableFields getTableFields() {
        HashMap<String, String> fields = new HashMap<>();
        fields.put(COMMAND, DataTypes.STRING);
        fields.put(TYPE, DataTypes.STRING);
        fields.put(OFFSET, DataTypes.INTEGER);
        fields.put(PERIOD, DataTypes.INTEGER);
        fields.put(RESET, DataTypes.STRING);
        return new TableFields(fields, PRIMARY_KEYS);
    }
}
