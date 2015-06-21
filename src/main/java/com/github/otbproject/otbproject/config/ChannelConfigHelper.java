package com.github.otbproject.otbproject.config;

import com.github.otbproject.otbproject.user.UserLevel;

public class ChannelConfigHelper {
    public static int getCooldown(ChannelConfig config, UserLevel ul) {
        switch (ul) {
            case DEFAULT:
                return config.userCooldowns.getUl_default();
            case SUBSCRIBER:
                return config.userCooldowns.getUl_subscriber();
            case REGULAR:
                return config.userCooldowns.getUl_regular();
            case MODERATOR:
                return config.userCooldowns.getUl_moderator();
            case SUPER_MODERATOR:
                return config.userCooldowns.getUl_super_moderator();
            case BROADCASTER:
                return config.userCooldowns.getUl_broadcaster();
            case INTERNAL:
                return config.userCooldowns.getUl_internal();
            case TOO_HIGH:
            default:
                return 0;
        }
    }

    public static ChannelConfig getCopy(ChannelConfig config) {
        ChannelConfig copy = new ChannelConfig();

        copy.setCommandCooldown(config.getCommandCooldown());
        copy.userCooldowns = copy.new UserCooldowns();
        if (config.userCooldowns == null) {
            config.userCooldowns = config.new UserCooldowns();
        }
        copy.userCooldowns.setUl_internal(config.userCooldowns.getUl_internal());
        copy.userCooldowns.setUl_broadcaster(config.userCooldowns.getUl_broadcaster());
        copy.userCooldowns.setUl_super_moderator(config.userCooldowns.getUl_super_moderator());
        copy.userCooldowns.setUl_moderator(config.userCooldowns.getUl_moderator());
        copy.userCooldowns.setUl_regular(config.userCooldowns.getUl_regular());
        copy.userCooldowns.setUl_subscriber(config.userCooldowns.getUl_subscriber());
        copy.userCooldowns.setUl_default(config.userCooldowns.getUl_default());
        copy.setDebug(config.isDebug());
        copy.setEnabled(config.isEnabled());
        copy.queueLimits = copy.new QueueLimits();
        if (config.queueLimits == null) {
            config.queueLimits = config.new QueueLimits();
        }
        copy.queueLimits.setHighPriorityLimit(config.queueLimits.getHighPriorityLimit());
        copy.queueLimits.setDefaultPriorityLimit(config.queueLimits.getDefaultPriorityLimit());
        copy.queueLimits.setLowPriorityLimit(config.queueLimits.getLowPriorityLimit());
        copy.setSilenced(config.isSilenced());
        return copy;
    }
}
