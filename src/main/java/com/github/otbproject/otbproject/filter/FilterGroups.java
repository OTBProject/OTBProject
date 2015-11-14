package com.github.otbproject.otbproject.filter;

import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.user.UserLevel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class FilterGroups {
    private FilterGroups() {}

    public static Optional<FilterGroup> get(DatabaseWrapper db, String groupName) {
        return db.getRecord(FilterGroupFields.TABLE_NAME, groupName,
                FilterGroupFields.NAME, FilterGroups::getFilterGroupFromResultSet);
    }

    public static List<FilterGroup> getFilterGroups(DatabaseWrapper db) {
        Optional<List<FilterGroup>> optional = db.tableDump(FilterGroupFields.TABLE_NAME,
                rs -> {
                    List<FilterGroup> filterGroups = new ArrayList<>();
                    while (rs.next()) {
                        filterGroups.add(getFilterGroupFromResultSet(rs));
                    }
                    return filterGroups;
                });
        return optional.orElse(Collections.emptyList());
    }

    // The Map is implemented as a HashMap, but it returns a generic Map
    public static ConcurrentMap<String, FilterGroup> getFilterGroupsMap(DatabaseWrapper db) {
        return getFilterGroups(db).stream().collect(Collectors.toConcurrentMap(FilterGroup::getName, filterGroup -> filterGroup));
    }

    private static FilterGroup getFilterGroupFromResultSet(ResultSet rs) throws SQLException {
        FilterGroup group = new FilterGroup();
        group.setName(rs.getString(FilterGroupFields.NAME));
        group.setUserLevel(UserLevel.valueOf(rs.getString(FilterGroupFields.USER_LEVEL)));
        group.setResponseCommand(rs.getString(FilterGroupFields.RESPONSE_COMMAND));
        group.setEnabled(Boolean.valueOf(rs.getString(FilterGroupFields.ENABLED)));
        return group;
    }

    public static boolean update(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.updateRecord(FilterGroupFields.TABLE_NAME, map.get(FilterGroupFields.NAME), FilterGroupFields.NAME, map);
    }

    public static boolean exists(DatabaseWrapper db, String groupName) {
        return db.exists(FilterGroupFields.TABLE_NAME, groupName, FilterGroupFields.NAME);
    }

    public static boolean add(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.insertRecord(FilterGroupFields.TABLE_NAME, map);
    }

    public static boolean remove(DatabaseWrapper db, String groupName) {
        return db.removeRecord(FilterGroupFields.TABLE_NAME, groupName, FilterGroupFields.NAME);
    }

    public static boolean addFilterGroupFromObj(DatabaseWrapper db, FilterGroup group) {
        HashMap<String, Object> map = new HashMap<>();

        map.put(FilterGroupFields.NAME, group.getName());
        map.put(FilterGroupFields.USER_LEVEL, group.getUserLevel().name());
        map.put(FilterGroupFields.RESPONSE_COMMAND, group.getResponseCommand());
        map.put(FilterGroupFields.ENABLED, String.valueOf(group.isEnabled()));

        if (exists(db, group.getName())) {
            return update(db, map);
        } else {
            return add(db, map);
        }
    }
}
