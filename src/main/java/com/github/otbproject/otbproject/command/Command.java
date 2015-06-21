package com.github.otbproject.otbproject.command;

import com.github.otbproject.otbproject.user.UserLevel;

import javax.validation.constraints.NotNull;

public class Command {
    @NotNull
    private String name;
    private String response = "example response";
    private UserLevel execUserLevel = UserLevel.DEFAULT;
    private int minArgs = 0;
    private int count = 0;

    public ModifyingUserLevels modifyingUserLevels = new ModifyingUserLevels();

    public class ModifyingUserLevels {
        private UserLevel nameModifyingUL = UserLevel.DEFAULT;
        private UserLevel responseModifyingUL = UserLevel.DEFAULT;
        private UserLevel userLevelModifyingUL = UserLevel.DEFAULT;

        public UserLevel getNameModifyingUL() {
            return nameModifyingUL;
        }

        public void setNameModifyingUL(UserLevel nameModifyingUL) {
            this.nameModifyingUL = nameModifyingUL;
        }

        public UserLevel getResponseModifyingUL() {
            return responseModifyingUL;
        }

        public void setResponseModifyingUL(UserLevel responseModifyingUL) {
            this.responseModifyingUL = responseModifyingUL;
        }

        public UserLevel getUserLevelModifyingUL() {
            return userLevelModifyingUL;
        }

        public void setUserLevelModifyingUL(UserLevel userLevelModifyingUL) {
            this.userLevelModifyingUL = userLevelModifyingUL;
        }
    }

    private String script;
    private boolean enabled = true;
    private boolean debug = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public UserLevel getExecUserLevel() {
        return execUserLevel;
    }

    public void setExecUserLevel(UserLevel execUserLevel) {
        this.execUserLevel = execUserLevel;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public void setMinArgs(int minArgs) {
        this.minArgs = minArgs;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
