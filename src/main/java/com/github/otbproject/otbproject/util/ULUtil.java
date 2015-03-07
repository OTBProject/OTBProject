package com.github.otbproject.otbproject.util;


import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.users.UserLevel;
import com.github.otbproject.otbproject.users.Users;

/**
 * Created by Justin on 06/03/2015.
 */
public class ULUtil {

    public static UserLevel getUserLevel(DatabaseWrapper db, String channel, String user) {
        if (user.equals(channel)) {
            return UserLevel.BROADCASTER;
        }

        UserLevel ul = null;
        if (Users.exists(db, user)) {
            ul = Users.get(db, user).getUserLevel();
        }

        if (ul == UserLevel.SUPER_MODERATOR) {
            return ul;
        }
        if(App.bot.getUserChannelDao().getChannel(channel).isOp(App.bot.getUserChannelDao().getUser(user))){
            return UserLevel.MODERATOR;
        }
        if ((ul == UserLevel.MODERATOR) || ul == UserLevel.IGNORED) {
            return ul;
        }
        if (App.bot.channels.get(channel).subscriberStorage.contains(user)) {
            App.bot.channels.get(channel).subscriberStorage.remove(user);
            return UserLevel.SUBSCRIBER;
        }

        // Default
        return UserLevel.DEFAULT;
    }
}