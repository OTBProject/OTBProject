package com.github.otbproject.otbproject.fs;

import com.github.otbproject.otbproject.config.DefaultConfigGenerator;
import com.github.otbproject.otbproject.fs.groups.Base;
import com.github.otbproject.otbproject.fs.groups.Chan;
import com.github.otbproject.otbproject.util.JsonHandler;
import com.github.otbproject.otbproject.util.preload.LoadStrategy;
import com.github.otbproject.otbproject.util.preload.PreloadLoader;

import java.io.File;
import java.io.IOException;

public class Setup {
    public static void setup() throws IOException {
        // Aliases Directory
        createLoadingTree(FSUtil.aliasesDir());

        // Commands Directory
        createLoadingTree(FSUtil.commandsDir());

        // Config Directory
        createDirs(FSUtil.configDir());
        createAccountFiles();

        // Data Directory
        createDirs(FSUtil.dataDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL);
        createDirs(FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS);
        // Create bot database
        String mainDBPath = FSUtil.dataDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DatabaseNames.MAIN;
        File mainDB = new File(mainDBPath);
        if (!mainDB.exists()) {
            if (!mainDB.createNewFile()) {
                throw new IOException("Unable to create database file: " + mainDBPath);
            }
            //Stream.of(Base.values()).forEach(base -> PreloadLoader.loadDirectory(base, Chan.BOT, null, LoadStrategy.FROM_LOADED));
            PreloadLoader.loadDirectory(Base.CMD, Chan.BOT, null, LoadStrategy.FROM_LOADED);
            PreloadLoader.loadDirectory(Base.ALIAS, Chan.BOT, null, LoadStrategy.FROM_LOADED);
        }

        // Defaults Directory
        createDirs(FSUtil.defaultsDir());

        // Filters Directory
        createLoadingTree(FSUtil.filtersDir());

        // Filter Groups Directory
        createLoadingTree(FSUtil.filterGroupsDir());

        // Logs Directory
        createDirs(FSUtil.logsDir());

        // Scripts Directory
        createDirs(FSUtil.commandScriptDir());
        createDirs(FSUtil.scriptLibsDir());
        createDirs(FSUtil.termScriptDir());
        createDirs(FSUtil.filterScriptDir());

        // Web Directory
        createDirs(FSUtil.webDir());
    }

    public static void setupChannel(String channel) throws IOException {
        if ((!new File(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.CHANNELS).exists())
                || (!new File(FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS).exists())) {
            setup(); // Because presumably it's not set up properly
        }

        // Aliases
        createChannelLoadingTree(FSUtil.aliasesDir(), channel);

        // Commands
        createChannelLoadingTree(FSUtil.commandsDir(), channel);

        // Data
        createDirs(FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel);
        // Create main database
        String mainDBPath = FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DatabaseNames.MAIN;
        File mainDB = new File(mainDBPath);
        if (!mainDB.exists()) {
            if (!mainDB.createNewFile()) {
                throw new IOException("Unable to create database file: " + mainDBPath);
            } else {
/*                Stream.of(Base.values()).forEach(base -> {
                    PreloadLoader.loadDirectory(base, Chan.ALL, channel, LoadStrategy.FROM_LOADED);
                    PreloadLoader.loadDirectory(base, Chan.SPECIFIC, channel, LoadStrategy.FROM_LOADED);
                });*/
                PreloadLoader.loadDirectory(Base.CMD, Chan.ALL, channel, LoadStrategy.FROM_LOADED);
                PreloadLoader.loadDirectory(Base.CMD, Chan.SPECIFIC, channel, LoadStrategy.FROM_LOADED);
                PreloadLoader.loadDirectory(Base.ALIAS, Chan.ALL, channel, LoadStrategy.FROM_LOADED);
                PreloadLoader.loadDirectory(Base.ALIAS, Chan.SPECIFIC, channel, LoadStrategy.FROM_LOADED);
            }
        }
        // Create quotes database
        String quotesDBPath = FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DatabaseNames.MAIN;
        File quotesDB = new File(quotesDBPath);
        if (!quotesDB.exists() && !quotesDB.createNewFile()) {
            throw new IOException("Unable to create database file: " + quotesDBPath);
        }

        // Filters
        createChannelLoadingTree(FSUtil.filtersDir(), channel);

        // Filter Groups
        createChannelLoadingTree(FSUtil.filterGroupsDir(), channel);
    }

    private static void createDirs(String path) throws IOException {
        File dirPath = new File(path);
        if ((!dirPath.exists()) && (!dirPath.mkdirs())) {
            throw new IOException(FSUtil.ERROR_MSG + path);
        }
    }

    public static void createAccountFiles() {
        String twitchAcctPath = FSUtil.configDir() + File.separator + FSUtil.ConfigFileNames.ACCOUNT_TWITCH;
        String beamAcctPath = FSUtil.configDir() + File.separator + FSUtil.ConfigFileNames.ACCOUNT_BEAM;
        if (!new File(twitchAcctPath).exists()) {
            JsonHandler.writeValue(twitchAcctPath, DefaultConfigGenerator.createAccountConfig());
        }
        if (!new File(beamAcctPath).exists()) {
            JsonHandler.writeValue(beamAcctPath, DefaultConfigGenerator.createAccountConfig());
        }
    }

    private static void createLoadingTree(String directory) throws IOException {
        createDirs(directory + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.LOADED);
        createDirs(directory + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(directory + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.FAILED);
        createDirs(directory + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.LOADED);
        createDirs(directory + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(directory + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.FAILED);
        createDirs(directory + File.separator + FSUtil.DirNames.CHANNELS);
    }

    private static void createChannelLoadingTree(String directory, String channel) throws IOException {
        createDirs(directory + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.LOADED);
        createDirs(directory + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(directory + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.FAILED);
    }
}
