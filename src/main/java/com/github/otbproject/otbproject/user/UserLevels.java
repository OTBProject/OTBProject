package com.github.otbproject.otbproject.user;


import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.messages.internal.InternalMessageSender;

import java.util.Optional;

public class UserLevels {
    private UserLevels() {}

    public static UserLevel getUserLevel(DatabaseWrapper db, String channel, String user) {
        if (user.startsWith(InternalMessageSender.DESTINATION_PREFIX)) {
            return UserLevel.INTERNAL;
        }

        if (user.equalsIgnoreCase(channel)) {
            return UserLevel.BROADCASTER;
        }

        UserLevel ul = null;
        Optional<User> optional = Users.get(db, user);
        if (optional.isPresent()) {
            ul = optional.get().getUserLevel();
        }

        if (ul == UserLevel.SUPER_MODERATOR) {
            return ul;
        }
        if (Control.bot().isUserMod(channel, user)) {
            return UserLevel.MODERATOR;
        }
        if ((ul == UserLevel.REGULAR) || ul == UserLevel.IGNORED) {
            return ul;
        }
        if (Control.bot().isUserSubscriber(channel, user)) {
            return UserLevel.SUBSCRIBER;
        }

        // Default
        return UserLevel.DEFAULT;
    }
}
