package com.github.otbproject.otbproject.config;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.util.JsonHandler;

import java.io.File;
import java.util.Optional;

public class Configs {
    private static String accountFileName = "";

    public static final String GENERAL_CONFIG_PATH = FSUtil.configDir() + File.separator + FSUtil.ConfigFileNames.GENERAL_CONFIG;
    public static final String BOT_CONFIG_PATH = FSUtil.dataDir() + File.separator + FSUtil.DirNames.BOT_CHANNEL + File.separator + FSUtil.ConfigFileNames.BOT_CONFIG;

    // Reading
    public static Account readAccount() {
        Account account = ConfigValidator.validateAccount(JsonHandler.readValue(getAccountPath(), Account.class));
        writeAccount(account);
        return account;
    }

    public static GeneralConfig readGeneralConfig() {
        GeneralConfig config = ConfigValidator.validateGeneralConfig(JsonHandler.readValue(GENERAL_CONFIG_PATH, GeneralConfig.class));
        writeGeneralConfig(config);
        return config;
    }

    public static BotConfig readBotConfig() {
        BotConfig config = ConfigValidator.validateBotConfig(JsonHandler.readValue(BOT_CONFIG_PATH, BotConfig.class));
        writeBotConfig(config);
        return config;
    }

    public static ChannelConfig readChannelConfig(String channel) {
        ChannelConfig config = ConfigValidator.validateChannelConfig(JsonHandler.readValue(getChannelPath(channel), ChannelConfig.class));
        writeChannelConfig(config, channel);
        return config;
    }

    // Writing
    private static void writeAccount(Account account) {
        JsonHandler.writeValue(getAccountPath(), account);
    }

    public static void writeAccount() {
        writeAccount(getAccount());
    }

    private static void writeGeneralConfig(GeneralConfig config) {
        JsonHandler.writeValue(GENERAL_CONFIG_PATH, config);
    }

    public static void writeGeneralConfig() {
        writeGeneralConfig(getGeneralConfig());
    }

    private static void writeBotConfig(BotConfig config) {
        JsonHandler.writeValue(BOT_CONFIG_PATH, config);
    }

    public static void writeBotConfig() {
        writeBotConfig(getBotConfig());
    }

    private static void writeChannelConfig(ChannelConfig config, String channel) {
        JsonHandler.writeValue(getChannelPath(channel), config);
    }

    public static void writeChannelConfig(String channel) {
        writeChannelConfig(getChannelConfig(channel), channel);
    }

    // Getting
    public static Account getAccount() {
        return App.configManager.getAccount();
    }

    public static GeneralConfig getGeneralConfig() {
        return App.configManager.getGeneralConfig();
    }

    public static BotConfig getBotConfig() {
        return App.configManager.getBotConfig();
    }

    public static ChannelConfig getChannelConfig(String channel) {
        Optional<Channel> optional = Channels.get(channel);
        if (!optional.isPresent()) {
            return null;
        }
        return optional.get().getConfig();
    }

    // Misc
    public static String getAccountFileName() {
        if (!accountFileName.equals("")) {
            return accountFileName;
        }

        ServiceName serviceName = getGeneralConfig().getServiceName();
        if (serviceName == ServiceName.BEAM) {
            return FSUtil.ConfigFileNames.ACCOUNT_BEAM;
        } else { // Defaults to Twitch
            return FSUtil.ConfigFileNames.ACCOUNT_TWITCH;
        }
    }

    public static void setAccountFileName(String accountFileName) {
        Configs.accountFileName = accountFileName;
    }

    private static String getAccountPath() {
        return FSUtil.configDir() + File.separator + getAccountFileName();
    }

    private static String getChannelPath(String channel) {
        return FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS + File.separator + channel + File.separator + FSUtil.ConfigFileNames.CHANNEL_CONFIG;
    }
}
