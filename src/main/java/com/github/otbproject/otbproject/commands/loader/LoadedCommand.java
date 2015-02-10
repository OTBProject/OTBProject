package com.github.otbproject.otbproject.commands.loader;

import com.github.otbproject.otbproject.users.UserLevel;

public class LoadedCommand {
    private String name;
    private String response;
    private UserLevel execUserLevel;
    private int minArgs;

    public ModifyingUserLevels modifyingUserLevels;

    public class ModifyingUserLevels {
        private UserLevel nameModifyingUL;
        private UserLevel responseModifyingUL;
        private UserLevel userLevelModifyingUL;

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
    private boolean enabled;
    private boolean debug;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

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

    public ModifyingUserLevels getModifyingUserLevels() {
        return modifyingUserLevels;
    }

    public void setModifyingUserLevels(ModifyingUserLevels modifyingUserLevels) {
        this.modifyingUserLevels = modifyingUserLevels;
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
}
