package com.github.otbproject.otbproject.config;

import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.channel.ChannelNotFoundException;
import com.github.otbproject.otbproject.channel.ChannelProxy;
import com.github.otbproject.otbproject.fs.FSUtil;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class Configs {

    private static final String GENERAL_CONFIG_PATH = FSUtil.configDir() + File.separator + FSUtil.ConfigFileNames.GENERAL_CONFIG;
    private static final String WEB_CONFIG_PATH = FSUtil.configDir() + File.separator + FSUtil.ConfigFileNames.WEB_CONFIG;

    private static String accountFileName = "";
    private static final AtomicReference<WrappedConfig<Account>> account = new AtomicReference<>();

    private static final WrappedConfig<GeneralConfig> generalConfig = WrappedConfig.of(GeneralConfig.class, GENERAL_CONFIG_PATH, GeneralConfig::new);
    private static final WrappedConfig<WebConfig> webConfig = WrappedConfig.of(WebConfig.class, WEB_CONFIG_PATH, WebConfig::new);

    private static final UpdatingConfig<BotConfig> botConfig;
    private static final WrappedConfig<BotConfig> botConfigProxy;

    static {
        botConfig = UpdatingConfig.create(BotConfig.class, FSUtil.configDir(), FSUtil.ConfigFileNames.BOT_CONFIG, BotConfig::new);
        botConfig.startMonitoring();
        botConfigProxy = botConfig.asWrappedConfig();
    }

    private Configs() {}

    // Update
    public static void reloadAccount() { // TODO rename
        account.set(WrappedConfig.of(Account.class, getAccountPath(), Account::new));
    }

    // Config getters
    public static WrappedConfig<Account> getAccount() {
        return account.get();
    }

    public static WrappedConfig<GeneralConfig> getGeneralConfig() {
        return generalConfig;
    }

    public static WrappedConfig<WebConfig> getWebConfig() {
        return webConfig;
    }

    public static WrappedConfig<BotConfig> getBotConfig() {
        return botConfigProxy;
    }

    public static WrappedConfig<ChannelConfig> getChannelConfig(String channel) throws ChannelNotFoundException {
        return Control.bot().channelManager().getOrThrow(channel).getConfig();
    }

    public static WrappedConfig<ChannelConfig> getChannelConfig(ChannelProxy channel) {
        return channel.getConfig();
    }

    // Misc
    private static String getAccountFileName() {
        if (!accountFileName.equals("")) {
            return accountFileName;
        }

        // If getting service exactly fails for some reason, get it inexactly
        Service service = generalConfig.getExactlyAsOptional(GeneralConfig::getService)
                .orElse(generalConfig.get(GeneralConfig::getService));
        switch (service) {
            case BEAM:
                return FSUtil.ConfigFileNames.ACCOUNT_BEAM;
            case TWITCH:
            default:
                return FSUtil.ConfigFileNames.ACCOUNT_TWITCH;
        }
    }

    public static void setAccountFileName(String accountFileName) {
        Configs.accountFileName = accountFileName;
    }

    private static String getAccountPath() {
        return FSUtil.configDir() + File.separator + getAccountFileName();
    }
}
