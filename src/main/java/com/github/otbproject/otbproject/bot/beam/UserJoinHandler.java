package com.github.otbproject.otbproject.bot.beam;

import pro.beam.api.resource.chat.events.EventHandler;
import pro.beam.api.resource.chat.events.UserJoinEvent;

class UserJoinHandler implements EventHandler<UserJoinEvent> {
    private final BeamChatChannel beamChatChannel;

    public UserJoinHandler(BeamChatChannel beamChatChannel) {
        this.beamChatChannel = beamChatChannel;
    }

    @Override
    public void onEvent(UserJoinEvent event) {
        beamChatChannel.userRoles.put(event.data.username.toLowerCase(), event.data.roles);
    }
}
