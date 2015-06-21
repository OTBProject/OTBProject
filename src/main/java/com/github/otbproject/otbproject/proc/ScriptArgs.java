package com.github.otbproject.otbproject.proc;

import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.user.UserLevel;

public class ScriptArgs {
    public DatabaseWrapper db;
    public String commandName;
    public String[] argsList;
    public String channel;
    public String destinationChannel;
    public String user;
    public UserLevel userLevel;

    public ScriptArgs(DatabaseWrapper db, String commandName, String[] argsList, String channel, String destinationChannel, String user, UserLevel userLevel) {
        this.db = db;
        this.commandName = commandName;
        this.argsList = argsList;
        this.channel = channel;
        this.destinationChannel = destinationChannel;
        this.user = user;
        this.userLevel = userLevel;
    }
}
