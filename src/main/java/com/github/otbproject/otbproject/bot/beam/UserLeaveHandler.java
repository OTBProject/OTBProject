package com.github.otbproject.otbproject.bot.beam;

import pro.beam.api.resource.chat.events.EventHandler;
import pro.beam.api.resource.chat.events.UserLeaveEvent;

class UserLeaveHandler implements EventHandler<UserLeaveEvent> {
    private final BeamChatChannel beamChatChannel;

    public UserLeaveHandler(BeamChatChannel beamChatChannel) {
        this.beamChatChannel = beamChatChannel;
    }

    @Override
    public void onEvent(UserLeaveEvent event) {
        beamChatChannel.userRoles.remove(event.data.username.toLowerCase());
    }
}

