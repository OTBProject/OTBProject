package com.github.otbproject.otbproject.filter;

import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.script.ScriptProcessor;
import com.github.otbproject.otbproject.user.UserLevel;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public class FilterProcessor {
    static final String METHOD_NAME = "checkMessage";
    static final ScriptProcessor PROCESSOR = new ScriptProcessor(true);
    private static final Comparator<FilterGroup> COMPARATOR = (o1, o2) ->
            Objects.compare(o1.getAction().severity, o2.getAction().severity, Comparator.<Integer>reverseOrder());


    // TODO delete
    public static FilterGroup process(ConcurrentHashMap.KeySetView<Filter, Boolean> filters, ConcurrentMap<String, FilterGroup> filterGroups, String message, UserLevel userLevel) {
        if (userLevel.getValue() < UserLevel.MODERATOR.getValue()) {
            return null;
        }
        Optional<Filter> filterOptional = filters.stream()
                .filter(Filter::isEnabled)
                .filter(filter -> {
                    FilterGroup group = filterGroups.get(filter.getGroup());
                    return (group != null) && (group.isEnabled()) && (group.getUserLevel().getValue() >= userLevel.getValue());
                })
                .filter(filter -> filter.matches(message))
                .findAny();
        if (!filterOptional.isPresent()) {
            return null;
        }
        return filterGroups.get(filterOptional.get().getGroup());
    }

    // Returns the FilterGroup of a Filter which matches the message, or null if no Filter matches
    public static FilterGroup process(ConcurrentMap<String, GroupFilterSet> groupFilterSets, String message, UserLevel userLevel) {
        if (userLevel.getValue() < UserLevel.MODERATOR.getValue()) {
            return null;
        }
        Optional<FilterGroup> entryOptional = groupFilterSets.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(groupFilterSet -> groupFilterSet.group.isEnabled())
                .filter(groupFilterSet -> groupFilterSet.group.getUserLevel().getValue() >= userLevel.getValue())
                .filter(groupFilterSet -> groupFilterSet.filterSet.stream()
                        .filter(Filter::isEnabled)
                        .filter(filter -> filter.matches(message))
                        .findAny().isPresent()
                )
                .map(set -> set.group)
                .sorted(COMPARATOR)
                .findFirst();
        return entryOptional.isPresent() ? entryOptional.get() : null;
    }

    public static void clearScriptCache() {
        PROCESSOR.clearScriptCache();
    }

    public static void cacheScripts() {
        File[] files = new File(FSUtil.filterScriptDir()).listFiles();
        if (files == null) {
            return;
        }
        Stream.of(files).filter(File::isFile).forEach(file -> PROCESSOR.cache(file.getName(), file.getPath()));
    }
}
