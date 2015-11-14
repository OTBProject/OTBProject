package com.github.otbproject.otbproject.bot;

import com.github.otbproject.otbproject.channel.ChannelNotFoundException;
import com.github.otbproject.otbproject.channel.ChannelProxy;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.user.UserLevel;
import com.github.otbproject.otbproject.user.UserLevels;

public class BotUtil {
    private BotUtil() {}

    public static boolean isModOrHigher(String channelName, String user) throws ChannelNotFoundException {
        // Check if user has user level mod or higher
        ChannelProxy channel = Control.bot().channelManager().getOrThrow(channelName);
        DatabaseWrapper db = channel.getMainDatabaseWrapper();
        UserLevel ul = UserLevels.getUserLevel(db, channelName, user);
        return ul.getValue() >= UserLevel.MODERATOR.getValue();
    }
}
