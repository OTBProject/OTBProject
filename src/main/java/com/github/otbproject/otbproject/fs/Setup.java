package com.github.otbproject.otbproject.fs;

import com.github.otbproject.otbproject.commands.loader.FSCommandLoader;
import com.github.otbproject.otbproject.commands.loader.LoadingSet;
import com.github.otbproject.otbproject.config.DefaultConfigGenerator;
import com.github.otbproject.otbproject.util.JsonHandler;

import java.io.File;
import java.io.IOException;

public class Setup {
    public static void setup() throws IOException {
        // Aliases Directory
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.LOADED);
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.FAILED);
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.LOADED);
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.FAILED);
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.CHANNELS);

        // Commands Directory
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.LOADED);
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.ALL_CHANNELS + File.separator + FSUtil.DirNames.FAILED);
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.LOADED);
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.DirNames.FAILED);
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.CHANNELS);

        // Config Directory
        createDirs(FSUtil.configDir());
        // createAccountFiles(); TODO uncomment in later release

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
            FSCommandLoader.LoadLoadedBotCommands();
            FSCommandLoader.LoadLoadedBotAliases();
        }

        // Defaults Directory
        createDirs(FSUtil.defaultsDir());

        // Logs Directory
        createDirs(FSUtil.logsDir());

        // Scripts Directory
        createDirs(FSUtil.scriptDir());
    }

    public static void setupChannel(String channel) throws IOException {
        if ((!new File(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.CHANNELS).exists())
                || (!new File(FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS).exists())) {
            setup(); // Because presumably it's not set up properly
        }

        // Aliases
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.LOADED);
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(FSUtil.aliasesDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.FAILED);

        // Commands
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.LOADED);
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.TO_LOAD);
        createDirs(FSUtil.commandsDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DirNames.FAILED);

        // Data
        createDirs(FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel);
        // Create main database
        String mainDBPath = FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DatabaseNames.MAIN;
        File mainDB = new File(mainDBPath);
        if (!mainDB.exists()) {
            if (!mainDB.createNewFile()) {
                throw new IOException("Unable to create database file: " + mainDBPath);
            } else {
                FSCommandLoader.LoadLoadedCommands(channel, LoadingSet.BOTH);
                FSCommandLoader.LoadLoadedAliases(channel, LoadingSet.BOTH);
            }
        }
        // Create quotes database
        String quotesDBPath = FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.DatabaseNames.MAIN;
        File quotesDB = new File(quotesDBPath);
        if (!quotesDB.exists() && !quotesDB.createNewFile()) {
            throw new IOException("Unable to create database file: " + quotesDBPath);
        }
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
}
