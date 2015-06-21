package com.github.otbproject.otbproject.filter;

import javax.validation.constraints.NotNull;

public class BasicFilter {
    @NotNull
    private String data;
    @NotNull
    private FilterType type;
    private String group = "default";
    private boolean enabled = true;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public FilterType getType() {
        return type;
    }

    public void setType(FilterType type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
