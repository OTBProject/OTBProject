package com.github.otbproject.otbproject.filter;

import com.github.otbproject.otbproject.database.DataTypes;
import com.github.otbproject.otbproject.database.TableFields;

import java.util.HashMap;
import java.util.HashSet;

public class FilterFields {
    public static final String DATA = "data";
    public static final String TYPE = "type";
    public static final String GROUP = "filterGroup";
    public static final String ENABLED = "enabled";

    public static final String TABLE_NAME = "tblFilters";
    public static final HashSet<String> PRIMARY_KEYS = new HashSet<>();

    static {
        PRIMARY_KEYS.add(DATA);
        PRIMARY_KEYS.add(TYPE);
    }

    public static TableFields getTableFields() {
        HashMap<String, String> map = new HashMap<>();
        map.put(DATA, DataTypes.STRING);
        map.put(TYPE, DataTypes.STRING);
        map.put(GROUP, DataTypes.STRING);
        map.put(ENABLED, DataTypes.STRING);
        return new TableFields(map, PRIMARY_KEYS);
    }
}
