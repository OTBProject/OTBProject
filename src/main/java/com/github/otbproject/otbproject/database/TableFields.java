package com.github.otbproject.otbproject.database;

import java.util.HashMap;
import java.util.HashSet;

public class TableFields {
    public final HashMap<String, String> map;
    public final HashSet<String> primaryKeys;

    public TableFields(HashMap<String, String> map, HashSet<String> primaryKeys) {
        this.map = map;
        this.primaryKeys = primaryKeys;
    }
}
