package com.github.otbproject.otbproject.fs;

import java.io.File;

public class FSUtil {
    public static final String ERROR_MSG = "Failed to create directory: ";
    private static final String BASE_DIR_NAME = ".otbproject";
    public static final String BASE_DIR_DEFAULT = System.getProperty("user.home") + File.separator + BASE_DIR_NAME;
    private static final String ASSETS_DIR_NAME = "assets";
    private static final String ALIASES_DIR_NAME = "aliases";
    private static final String COMMANDS_DIR_NAME = "commands";
    private static final String CONFIG_DIR_NAME = "config";
    private static final String DATA_DIR_NAME = "data";
    private static final String FILTERS_DIR_NAME = "filters";
    private static final String FILTER_GROUPS_DIR_NAME = "filter-groups";
    private static final String LOGS_DIR_NAME = "logs";
    private static final String SCRIPT_DIR_NAME = "scripts";
    private static final String LIBS_DIR_NAME = "libs";
    private static final String TERMS_DIR_NAME = "terms";
    private static final String WEB_DIR_NAME = "web";

    public static final String GUI_HISTORY_FILE = "gui-command-history.json";

    private static String baseDir = BASE_DIR_DEFAULT;

    public static String getBaseDir() {
        return baseDir;
    }

    // Assumes path does not have a trailing slash
    public static void setBaseDirPath(String path) {
        baseDir = path + File.separator + BASE_DIR_NAME;
    }

    public static String assetsDir() {
        return baseDir + File.separator + ASSETS_DIR_NAME;
    }

    public static String aliasesDir() {
        return baseDir + File.separator + ALIASES_DIR_NAME;
    }

    public static String commandsDir() {
        return baseDir + File.separator + COMMANDS_DIR_NAME;
    }

    public static String configDir() {
        return baseDir + File.separator + CONFIG_DIR_NAME;
    }

    public static String dataDir() {
        return baseDir + File.separator + DATA_DIR_NAME;
    }

    private static String filtersBaseDir() {
        return baseDir + File.separator + FILTERS_DIR_NAME;
    }

    public static String filtersDir() {
        return filtersBaseDir() + File.separator + FILTERS_DIR_NAME;
    }

    public static String filterGroupsDir() {
        return filtersBaseDir() + File.separator + FILTER_GROUPS_DIR_NAME;
    }

    public static String logsDir() {
        return baseDir + File.separator + LOGS_DIR_NAME;
    }

    public static String scriptDir() {
        return baseDir + File.separator + SCRIPT_DIR_NAME;
    }

    public static String commandScriptDir() {
        return scriptDir() + File.separator + COMMANDS_DIR_NAME;
    }

    public static String scriptLibsDir() {
        return scriptDir() + File.separator + LIBS_DIR_NAME;
    }

    public static String termScriptDir() {
        return scriptDir() + File.separator + TERMS_DIR_NAME;
    }

    public static String filterScriptDir() {
        return scriptDir() + File.separator + FILTERS_DIR_NAME;
    }

    public static String webDir() {
        return baseDir + File.separator + WEB_DIR_NAME;
    }

    public static class DirNames {
        public static final String ALL_CHANNELS = "all-channels";
        public static final String BOT_CHANNEL = "bot-channel";
        public static final String CHANNELS = "channels";

        public static final String LOADED = "loaded";
        public static final String TO_LOAD = "to-load";
        public static final String FAILED = "failed";
    }

    public static class DatabaseNames {
        public static final String MAIN = "main.db";
        public static final String QUOTES = "quotes.db";
    }

    public static class ConfigFileNames {
        public static final String ACCOUNT_TWITCH = "account-twitch.json";
        public static final String ACCOUNT_BEAM = "account-beam.json";
        public static final String GENERAL_CONFIG = "general-config.json";
        public static final String BOT_CONFIG = "bot-config.json";
        public static final String CHANNEL_CONFIG = "config.json";
        public static final String WEB_CONFIG = "web-config.json";
    }

    public static class Assets {
        public static final String LOGO = "logo.png";
    }
}
