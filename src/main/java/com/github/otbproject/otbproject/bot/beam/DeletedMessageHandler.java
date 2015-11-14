package com.github.otbproject.otbproject.bot.beam;

import pro.beam.api.resource.chat.events.DeleteMessageEvent;
import pro.beam.api.resource.chat.events.EventHandler;

class DeletedMessageHandler implements EventHandler<DeleteMessageEvent> {
    private final BeamChatChannel beamChatChannel;

    public DeletedMessageHandler(BeamChatChannel beamChatChannel) {
        this.beamChatChannel = beamChatChannel;
    }

    @Override
    public void onEvent(DeleteMessageEvent event) {
        beamChatChannel.messageCache.invalidate(event.data.id.toString());
    }
}
