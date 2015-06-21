package com.github.otbproject.otbproject.bot;

import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.database.Databases;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractBot implements IBot {
    protected final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();
    protected final DatabaseWrapper botDB = Databases.createBotDbWrapper();

    @Override
    public ConcurrentHashMap<String, Channel> getChannels() {
        return channels;
    }

    @Override
    public DatabaseWrapper getBotDB() {
        return botDB;
    }
}
