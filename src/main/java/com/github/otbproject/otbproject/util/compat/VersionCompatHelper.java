package com.github.otbproject.otbproject.util.compat;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.command.Command;
import com.github.otbproject.otbproject.command.Commands;
import com.github.otbproject.otbproject.config.GeneralConfig;
import com.github.otbproject.otbproject.database.DatabaseHelper;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.database.Databases;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.fs.PathBuilder;
import com.github.otbproject.otbproject.fs.groups.Base;
import com.github.otbproject.otbproject.fs.groups.Chan;
import com.github.otbproject.otbproject.fs.groups.Load;
import com.github.otbproject.otbproject.util.JsonHandler;
import com.github.otbproject.otbproject.util.version.Version;

import java.io.File;
import java.util.Optional;

public class VersionCompatHelper {
    private VersionCompatHelper() {}

    public static void urgentCompatFixes(Version oldVersion) {
        if (versionCheck(oldVersion)) {
            fixGeneralConfig();
        }
    }

    public static void normalCompatFixes(Version oldVersion) {
        if (versionCheck(oldVersion)) {
            fix1_1To2_0();
        }
    }

    private static boolean versionCheck(Version oldVersion) {
        return oldVersion.checker().major(1).minor(1).isVersion();
    }

    private static void fix1_1To2_0() {
        fixScripts();
        removeOldPreloads();
        removeOldVersionFile();
    }

    private static void fixScripts() {
        // Move scripts from base dir into subdirectory
        FSUtil.streamDirectory(new File(FSUtil.scriptDir()))
                .filter(File::isFile)
                .forEach(file -> file.renameTo(new File(FSUtil.commandScriptDir() + File.separator + file.getName())));
    }

    private static void fixGeneralConfig() {
        Optional<GeneralConfigOld> optional = JsonHandler.readValue((FSUtil.configDir() + File.separator + FSUtil.ConfigFileNames.GENERAL_CONFIG), GeneralConfigOld.class);
        if (!optional.isPresent()) {
            return;
        }
        GeneralConfigOld configOld = optional.get();
        GeneralConfig configNew = new GeneralConfig();

        configNew.setService(configOld.getServiceName());
        configNew.setPermanentlyEnabledCommands(configOld.permanently_enabled_commands);
        configNew.getPermanentlyEnabledCommands().add("!leave");
        JsonHandler.writeValue((FSUtil.configDir() + File.separator + FSUtil.ConfigFileNames.GENERAL_CONFIG), configNew);
    }

    private static void removeOldVersionFile() {
        deleteFile(FSUtil.configDir() + File.separator + "VERSION");
    }

    private static void removeOldPreloads() {
        String basePath = new PathBuilder().base(Base.CMD).channels(Chan.ALL).load(Load.ED).create();
        deleteFile(basePath + File.separator + "command.reset.count.success.json");
        deleteFile(basePath + File.separator + "reset-count.json");
        deleteFile(FSUtil.commandScriptDir() + File.separator + "ScriptResetCount.groovy");
        FSUtil.streamDirectory(new File(FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS))
                .map(File::getName)
                .peek(s -> App.logger.debug("Deleting old commands for channel: " + s))
                .map(Databases::createChannelMainDbWrapper)
                .forEach(db -> {
                    removeCommand(db, "~%command.reset.count.success");
                    removeCommand(db, "!resetCount");
                });
    }

    private static void removeCommand(DatabaseWrapper db, String command) {
        if (Commands.remove(db, command)) {
            App.logger.debug("Successfully removed old command: " + command);
        } else {
            App.logger.warn("Failed to remove old command: " + command);
        }
    }

    private static void deleteFile(String path) {
        if (new File(path).delete()) {
            App.logger.info("Deleted " + path);
        } else {
            App.logger.error("Failed to delete " + path);
        }
    }
}
