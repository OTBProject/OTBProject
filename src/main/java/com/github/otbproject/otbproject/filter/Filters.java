package com.github.otbproject.otbproject.filter;

import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.util.CustomCollectors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Filters {
    private Filters() {}

    public static Optional<BasicFilter> get(DatabaseWrapper db, String data, FilterType type) {
        List<Map.Entry<String, Object>> list = new ArrayList<>();
        list.add(new AbstractMap.SimpleImmutableEntry<>(FilterFields.DATA, data));
        list.add(new AbstractMap.SimpleImmutableEntry<>(FilterFields.TYPE, type.name()));
        return db.getRecord(FilterFields.TABLE_NAME, list, Filters::getFilterFromResultSet);
    }

    public static List<BasicFilter> getBasicFilters(DatabaseWrapper db) {
        Optional<List<BasicFilter>> optional = db.tableDump(FilterFields.TABLE_NAME,
                rs -> {
                    List<BasicFilter> filters = new ArrayList<>();
                    while (rs.next()) {
                        filters.add(getFilterFromResultSet(rs));
                    }
                    return filters;
                });
        return optional.orElse(Collections.emptyList());
    }

    public static ConcurrentHashMap.KeySetView<Filter, Boolean> getAllFilters(DatabaseWrapper db) {
        return getBasicFilters(db).stream().map(Filter::fromBasicFilter).collect(CustomCollectors.toConcurrentSet());
    }

    private static BasicFilter getFilterFromResultSet(ResultSet rs) throws SQLException {
        BasicFilter basicFilter = new BasicFilter();
        basicFilter.setData(rs.getString(FilterFields.DATA));
        basicFilter.setType(FilterType.valueOf(rs.getString(FilterFields.TYPE)));
        basicFilter.setGroup(rs.getString(FilterFields.GROUP));
        basicFilter.setEnabled(Boolean.valueOf(rs.getString(FilterFields.ENABLED)));
        return basicFilter;
    }

    public static boolean update(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.updateRecord(FilterFields.TABLE_NAME, map.get(FilterFields.DATA), FilterFields.DATA, map);
    }

    public static boolean exists(DatabaseWrapper db, String data, FilterType type) {
        List<Map.Entry<String, Object>> list = new ArrayList<>();
        list.add(new AbstractMap.SimpleImmutableEntry<>(FilterFields.DATA, data));
        list.add(new AbstractMap.SimpleImmutableEntry<>(FilterFields.TYPE, type.name()));
        return db.exists(FilterFields.TABLE_NAME, list);
    }

    public static boolean add(DatabaseWrapper db, HashMap<String, Object> map) {
        return db.insertRecord(FilterFields.TABLE_NAME, map);
    }

    public static boolean remove(DatabaseWrapper db, String data, FilterType type) {
        List<Map.Entry<String, Object>> list = new ArrayList<>();
        list.add(new AbstractMap.SimpleImmutableEntry<>(FilterFields.DATA, data));
        list.add(new AbstractMap.SimpleImmutableEntry<>(FilterFields.TYPE, type.name()));
        return db.removeRecord(FilterFields.TABLE_NAME, list);
    }

    public static boolean addFilterFromObj(DatabaseWrapper db, BasicFilter basicFilter) {
        HashMap<String, Object> map = new HashMap<>();

        map.put(FilterFields.DATA, basicFilter.getData());
        map.put(FilterFields.TYPE, basicFilter.getType().name());
        map.put(FilterFields.GROUP, basicFilter.getGroup());
        map.put(FilterFields.ENABLED, String.valueOf(basicFilter.isEnabled()));

        if (exists(db, basicFilter.getData(), basicFilter.getType())) {
            return update(db, map);
        } else {
            return add(db, map);
        }
    }
}
