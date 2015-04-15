package com.github.otbproject.otbproject.config;

public class ConfigValidator {
    public static Account validateAccount(Account account) {
        Account defaultAccount = DefaultConfigGenerator.createAccountConfig();

        if (account == null) {
            return defaultAccount;
        }

        Account validatedAccount = AccountHelper.getCopy(account);

        if (validatedAccount.getName() == null) {
            validatedAccount.setName(defaultAccount.getName());
        }

        if (validatedAccount.getPasskey() == null) {
            validatedAccount.setName(defaultAccount.getPasskey());
        }

        return validatedAccount;
    }

    public static GeneralConfig validateGeneralConfig(GeneralConfig config) {
        GeneralConfig defaultConfig = DefaultConfigGenerator.createGeneralConfig();

        if (config == null) {
            return defaultConfig;
        }

        GeneralConfig validatedConfig = GeneralConfigHelper.getCopy(config);

        if (validatedConfig.getServiceName() == null) {
            validatedConfig.setServiceName(defaultConfig.getServiceName());
        }

        if (validatedConfig.getIp_binding() == null) {
            validatedConfig.setIp_binding(defaultConfig.getIp_binding());
        }

        GeneralConfigHelper.initialize(validatedConfig);

        return validatedConfig;
    }

    public static BotConfig validateBotConfig(BotConfig config) {
        BotConfig defaultConfig = DefaultConfigGenerator.createBotConfig();

        if (config == null) {
            return defaultConfig;
        }

        BotConfig validatedConfig = BotConfigHelper.getCopy(config);

        if (validatedConfig.getChannelJoinSetting() == null) {
            validatedConfig.setChannelJoinSetting(defaultConfig.getChannelJoinSetting());
        }

        if (validatedConfig.getMessageSendDelayInMilliseconds() == null) {
            validatedConfig.setMessageSendDelayInMilliseconds(defaultConfig.getMessageSendDelayInMilliseconds());
        } else if (validatedConfig.getMessageSendDelayInMilliseconds() < 0) {
            validatedConfig.setMessageSendDelayInMilliseconds(0);
        }

        BotConfigHelper.initialize(validatedConfig);

        return validatedConfig;
    }

    public static ChannelConfig validateChannelConfig(ChannelConfig config) {
        ChannelConfig defaultConfig = DefaultConfigGenerator.createChannelConfig();

        if (config == null) {
            return defaultConfig;
        }

        ChannelConfig validatedConfig = ChannelConfigHelper.getCopy(config);

        if (validatedConfig.getCommandCooldown() == null) {
            validatedConfig.setCommandCooldown(defaultConfig.getCommandCooldown());
        }

        if (validatedConfig.userCooldowns.getUl_regular() == null) {
            validatedConfig.userCooldowns.setUl_regular(defaultConfig.userCooldowns.getUl_regular());
        }

        if (validatedConfig.userCooldowns.getUl_subscriber() == null) {
            validatedConfig.userCooldowns.setUl_subscriber(defaultConfig.userCooldowns.getUl_subscriber());
        }

        if (validatedConfig.userCooldowns.getUl_default() == null) {
            validatedConfig.userCooldowns.setUl_default(defaultConfig.userCooldowns.getUl_default());
        }

        if (validatedConfig.isEnabled() == null) {
            validatedConfig.setEnabled(defaultConfig.isEnabled());
        }

        if (validatedConfig.queueLimits.getHighPriorityLimit() == null) {
            validatedConfig.queueLimits.setHighPriorityLimit(defaultConfig.queueLimits.getHighPriorityLimit());
        }

        if (validatedConfig.queueLimits.getDefaultPriorityLimit() == null) {
            validatedConfig.queueLimits.setDefaultPriorityLimit(defaultConfig.queueLimits.getDefaultPriorityLimit());
        }

        return validatedConfig;
    }
}
