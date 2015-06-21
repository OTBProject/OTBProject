package com.github.otbproject.otbproject.command;

import com.github.otbproject.otbproject.user.UserLevel;

import javax.validation.constraints.NotNull;

public class Alias {
    @NotNull
    private String name;

    @NotNull
    private String command;

    private UserLevel modifyingUserLevel = UserLevel.DEFAULT;
    private boolean enabled = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public UserLevel getModifyingUserLevel() {
        return modifyingUserLevel;
    }

    public void setModifyingUserLevel(UserLevel modifyingUserLevel) {
        this.modifyingUserLevel = modifyingUserLevel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
