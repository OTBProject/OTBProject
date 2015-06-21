package com.github.otbproject.otbproject.filter;

import com.github.otbproject.otbproject.fs.FSUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.util.regex.Pattern;

public class Filter {
    private static final String ANY = ".*";
    // Custom word boundary to match if phrase doesn't end in \w
    private static final String WORD_BOUNDARY = "($|^|\\W|_)";

    private final String group;
    private final FilterType type;
    private final Pattern pattern;
    private final String data;
    private final boolean enabled;

    public Filter(String group, FilterType type, Pattern pattern, String data, boolean enabled) {
        this.group = group;
        this.type = type;
        this.pattern = pattern;
        this.data = data; // Script if script; original regex string otherwise
        this.enabled = enabled;
    }

    public String getGroup() {
        return group;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean matches(String message) {
        switch (type) {
            case PHRASE:
            case PLAINTEXT:
            case REGEX:
                return pattern.matcher(message).matches();
            case SCRIPT:
                // TODO possibly tweak method name and parameter(s) passed in
                return FilterProcessor.PROCESSOR.process(data, (FSUtil.filtersDir() + File.separator + data), FilterProcessor.METHOD_NAME, message, Boolean.class, false);
            // Default should never occur
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Filter)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Filter rhs = (Filter) obj;
        return new EqualsBuilder()
                .append(type, rhs.type)
                .append(data, rhs.data)
                .append(pattern, rhs.pattern)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(type)
                .append(data)
                .append(pattern)
                .toHashCode();
    }

    public BasicFilter toBasicFilter() {
        BasicFilter filter = new BasicFilter();
        filter.setGroup(group);
        filter.setType(type);
        filter.setData(data);
        filter.setEnabled(enabled);
        return filter;
    }

    public static Filter fromBasicFilter(BasicFilter filter) {
        Pattern pattern = null;
        switch (filter.getType()) {
            case PHRASE:
                pattern = Pattern.compile((ANY + WORD_BOUNDARY + Pattern.quote(filter.getData()) + WORD_BOUNDARY + ANY), Pattern.CASE_INSENSITIVE);
                break;
            case PLAINTEXT:
                pattern = Pattern.compile((ANY + Pattern.quote(filter.getData()) + ANY), Pattern.CASE_INSENSITIVE);
                break;
            case REGEX:
                pattern = Pattern.compile((ANY + filter.getData() + ANY), Pattern.CASE_INSENSITIVE);
                break;
        }
        return new Filter(filter.getGroup(), filter.getType(), pattern, filter.getData(), filter.isEnabled());
    }
}
