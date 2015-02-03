package com.github.opentwitchbotteam.otbproject.proc;

import com.github.opentwitchbotteam.otbproject.database.DatabaseWrapper;
import com.github.opentwitchbotteam.otbproject.users.User;
import com.github.opentwitchbotteam.otbproject.users.UserFields;
import com.github.opentwitchbotteam.otbproject.users.UserLevel;

import java.sql.SQLException;

public class Util {
    public static UserLevel getUserLevel(DatabaseWrapper db, String channel, String user, boolean subscriber) {
        try {
            if (User.exists(db, user)) {
                return UserLevel.valueOf((String)User.get(db, user, UserFields.USER_LEVEL));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (subscriber) {
            return UserLevel.SUBSCRIBER;
        }
        if (user.equals(channel)) {
            return UserLevel.BROADCASTER;
        }
        // TODO figure out how to find out if mod

        // Default
        return UserLevel.DEFAULT;
    }
}
