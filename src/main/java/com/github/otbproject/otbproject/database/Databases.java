package com.github.otbproject.otbproject.database;

import com.github.otbproject.otbproject.fs.FSUtil;

import java.io.File;

public class Databases {
    private Databases() {}

    public static DatabaseWrapper createChannelMainDbWrapper(String channel) {
        String path = FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DatabaseNames.MAIN;
        return SQLiteWrapper.createDatabase(path, DatabaseHelper.getMainTablesHashMap());
    }

    public static SQLiteQuoteWrapper createChannelQuoteDbWrapper(String channel) {
        String path = FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DatabaseNames.QUOTES;
        return SQLiteQuoteWrapper.createDatabase(path, DatabaseHelper.getQuoteTablesHashMap());
    }

    public static DatabaseWrapper createBotDbWrapper() {
        String path = FSUtil.dataDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DatabaseNames.MAIN;
        return SQLiteWrapper.createDatabase(path, DatabaseHelper.getMainTablesHashMap());
    }
}
