package com.github.otbproject.otbproject.user;


import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.messages.internal.InternalMessageSender;

public class UserLevels {

    public static UserLevel getUserLevel(DatabaseWrapper db, String channel, String user) {
        if (user.startsWith(InternalMessageSender.DESTINATION_PREFIX)) {
            return UserLevel.INTERNAL;
        }

        if (user.equalsIgnoreCase(channel)) {
            return UserLevel.BROADCASTER;
        }

        UserLevel ul = null;
        User userObj = Users.get(db, user);
        if (userObj != null) {
            ul = userObj.getUserLevel();
        }

        if (ul == UserLevel.SUPER_MODERATOR) {
            return ul;
        }
        if (Bot.getBot().isUserMod(channel, user)) {
            return UserLevel.MODERATOR;
        }
        if ((ul == UserLevel.REGULAR) || ul == UserLevel.IGNORED) {
            return ul;
        }
        if (Bot.getBot().isUserSubscriber(channel, user)) {
            return UserLevel.SUBSCRIBER;
        }

        // Default
        return UserLevel.DEFAULT;
    }
}
