package com.github.otbproject.otbproject.filters;

import com.github.otbproject.otbproject.database.DataTypes;
import com.github.otbproject.otbproject.database.TableFields;

import java.util.HashMap;

public class FilterGroupFields {
    public static final String NAME = "name";
    public static final String RESPONSE_COMMAND = "responseCmd";
    public static final String USER_LEVEL = "userLevel";

    public static final String TABLE_NAME = "tblFilterGroups";
    public static final String PRIMARY_KEY = NAME;

    public static TableFields getTableFields() {
        HashMap<String, String> map = new HashMap<>();
        map.put(NAME, DataTypes.STRING);
        map.put(RESPONSE_COMMAND, DataTypes.STRING);
        map.put(USER_LEVEL, DataTypes.STRING);
        return new TableFields(map, PRIMARY_KEY);
    }
}
