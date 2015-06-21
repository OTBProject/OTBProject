package com.github.otbproject.otbproject.filter;

import com.github.otbproject.otbproject.user.UserLevel;

import javax.validation.constraints.NotNull;

public class FilterGroup {
    @NotNull
    private String name;
    private String responseCommand = "~%filter.response.default";
    private UserLevel userLevel = UserLevel.SUBSCRIBER;
    private FilterAction action = FilterAction.PURGE;
    private boolean enabled = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(String responseCommand) {
        this.responseCommand = responseCommand;
    }

    public UserLevel getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(UserLevel userLevel) {
        this.userLevel = userLevel;
    }

    public FilterAction getAction() {
        return action;
    }

    public void setAction(FilterAction action) {
        this.action = action;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
