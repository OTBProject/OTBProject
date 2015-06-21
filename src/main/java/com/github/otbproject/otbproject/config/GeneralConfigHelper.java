package com.github.otbproject.otbproject.config;

import java.util.ArrayList;

public class GeneralConfigHelper {
    public static void initialize(GeneralConfig config) {
        if (config.permanently_enabled_commands == null) {
            config.permanently_enabled_commands = new ArrayList<>();
        }
    }

    public static boolean permanentlyEnable(GeneralConfig config, String command) {
        if (!config.permanently_enabled_commands.contains(command)) {
            config.permanently_enabled_commands.add(command);
            return true;
        }
        return false;
    }

    public static boolean impermanentlyEnable(GeneralConfig config, String command) {
        return config.permanently_enabled_commands.remove(command);
    }

    public static void clearPermanentlyEnabled(GeneralConfig config) {
        config.permanently_enabled_commands.clear();
    }

    public static boolean isPermanentlyEnabled(GeneralConfig config, String command) {
        return config.permanently_enabled_commands.contains(command);
    }

    public static GeneralConfig getCopy(GeneralConfig config) {
        GeneralConfig copy = new GeneralConfig();

        copy.setServiceName(config.getServiceName());
        copy.setPortNumber(config.getPortNumber());
        copy.setIp_binding(config.getIp_binding());

        if (config.permanently_enabled_commands == null) {
            copy.permanently_enabled_commands = null;
        } else {
            copy.permanently_enabled_commands = new ArrayList<>(config.permanently_enabled_commands);
        }

        return copy;
    }
}
