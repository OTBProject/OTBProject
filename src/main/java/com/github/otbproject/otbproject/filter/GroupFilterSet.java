package com.github.otbproject.otbproject.filter;

import com.github.otbproject.otbproject.App;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class GroupFilterSet {
    public final FilterGroup group;
    public final ConcurrentHashMap.KeySetView<Filter, Boolean> filterSet = ConcurrentHashMap.newKeySet();

    public GroupFilterSet(FilterGroup group) {
        this.group = group;
    }

    public static ConcurrentMap<String, GroupFilterSet> createGroupFilterSetMap(Collection<FilterGroup> filterGroups, Collection<Filter> filters) {
        ConcurrentMap<String, GroupFilterSet> map  = filterGroups.stream().collect(Collectors.toConcurrentMap(FilterGroup::getName, GroupFilterSet::new));
        filters.stream()
                .forEach(filter -> {
                    GroupFilterSet groupFilterSet = map.get(filter.getGroup());
                    if (groupFilterSet == null) {
                        BasicFilter basicFilter = filter.toBasicFilter();
                        App.logger.error("Filter Group with name '" + filter.getGroup() + "' not found. Unable to process Filter with type '"
                                + basicFilter.getType().name() + "' and data: " + basicFilter.getData());
                        return;
                    }
                    groupFilterSet.filterSet.add(filter);
                });
        return map;
    }
}
