package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.commands.loader.LoadedAlias;
import com.github.otbproject.otbproject.commands.loader.LoadedCommand;
import com.github.otbproject.otbproject.users.UserLevel;

public class ExampleCommandGenerator {

    public static LoadedCommand createExampleCommand() {
        LoadedCommand command = new LoadedCommand();
        command.setName("!example-command-name");
        command.setResponse("example response");
        command.setExecUserLevel(UserLevel.DEFAULT);
        command.setMinArgs(0);
        command.modifyingUserLevels = command.new ModifyingUserLevels();
        command.modifyingUserLevels.setNameModifyingUL(UserLevel.SUPER_MODERATOR);
        command.modifyingUserLevels.setResponseModifyingUL(UserLevel.DEFAULT);
        command.modifyingUserLevels.setUserLevelModifyingUL(UserLevel.INTERNAL);
        command.setScript("ScriptName.groovy");
        command.setEnabled(true);
        command.setDebug(false);

        return command;
    }

    public static LoadedAlias createExampleAlias() {
        LoadedAlias alias = new LoadedAlias();
        alias.setName("!example-alias-name");
        alias.setCommand("!example command");
        alias.setModifyingUserLevel(UserLevel.DEFAULT);
        alias.setEnabled(true);

        return alias;
    }
}
