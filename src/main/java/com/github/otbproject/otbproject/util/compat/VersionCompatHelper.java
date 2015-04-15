package com.github.otbproject.otbproject.util.compat;

import com.github.otbproject.otbproject.config.Account;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.util.JsonHandler;

import java.io.File;

public class VersionCompatHelper {
    public static void fixCompatIssues(String currentVersion, String oldVersion) {
        if (currentVersion.startsWith("1.1") && oldVersion.startsWith("1.0")) {
            fix1dot0To1dot1();
        }
    }

    private static void fix1dot0To1dot1() {
        String oldAccountFilePath = FSUtil.configDir() + File.separator + "account.json";
        String twitchAccountFilePath = FSUtil.configDir() + File.separator + FSUtil.ConfigFileNames.ACCOUNT_TWITCH;
        File oldAcctFile = new File(oldAccountFilePath);
        File twitchAcctFile = new File(twitchAccountFilePath);

        if (oldAcctFile.exists() && !twitchAcctFile.exists()) {
            AccountOld accountOld = JsonHandler.readValue(oldAccountFilePath, AccountOld.class);
            if (accountOld == null) {
                return;
            }
            Account accountNew = new Account();
            accountNew.setName(accountOld.getName());
            accountNew.setPasskey(accountOld.getOauth());
            JsonHandler.writeValue(twitchAccountFilePath, accountNew);
            // Not sure if need to recreate File for this
            if (new File(twitchAccountFilePath).exists()) {
                oldAcctFile.delete();
            }
        }
    }
}
