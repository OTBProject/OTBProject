package com.github.otbproject.otbproject.beam;

import com.github.otbproject.otbproject.App;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.channel.BeamChannel;
import pro.beam.api.resource.chat.BeamChat;
import pro.beam.api.resource.chat.BeamChatConnectable;
import pro.beam.api.resource.chat.events.IncomingMessageEvent;
import pro.beam.api.resource.chat.methods.AuthenticateMessage;
import pro.beam.api.resource.chat.methods.ChatSendMethod;
import pro.beam.api.services.impl.ChatService;
import pro.beam.api.services.impl.UsersService;

import java.util.concurrent.ExecutionException;

/**
 * Created by Justin on 05/04/2015.
 */
public class BeamChatChannel {
    BeamBot beamBot;
    BeamChat beamChat;
    BeamChatConnectable beamChatConnectable;

    public BeamChatChannel(String channelName){
        beamBot = ((BeamBot) App.bot);
        try {
            BeamChannel channel = beamBot.beamUser.channel;
            for(BeamUser user : beamBot.beam.use(UsersService.class).search(channelName).get()){
                if (user.username.equalsIgnoreCase(channelName)){
                     channel = beamBot.beam.use(UsersService.class).findOne(user.id).get().channel;
                    break;
                }
            }
            beamChat = beamBot.beam.use(ChatService.class).findOne(channel.id).get();
        } catch (InterruptedException | ExecutionException e) {
            App.logger.catching(e);
        }
        System.out.println(beamBot.beamUser);
        beamChatConnectable = beamChat.makeConnectable(beamBot.beam);
        boolean connected = false;
        try {
            connected = beamChatConnectable.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (connected) {
            beamChatConnectable.send(AuthenticateMessage.from(beamBot.beamUser.channel, beamBot.beamUser, beamChat.authkey));
            try {
                Thread.sleep(200);// needed to allow the authentication
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(2); // needed to ensure messages get sent in the right order
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                beamChatConnectable.send(ChatSendMethod.of("Test" + i));
            }
            beamChatConnectable.on(IncomingMessageEvent.class, new MessageHandler(channelName));

        }
    }
}
