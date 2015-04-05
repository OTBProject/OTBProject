package com.github.otbproject.otbproject.beam;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.IBot;
import com.github.otbproject.otbproject.api.APIConfig;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.chat.methods.ChatSendMethod;
import pro.beam.api.services.impl.UsersService;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by Justin on 05/04/2015.
 */
public class BeamBot implements IBot {

    BeamAPI beam = new BeamAPI();
    BeamUser beamUser;
    HashMap<String,BeamChatChannel> beamChannels = new HashMap<>();

    public BeamBot() {
        try {
            beamUser = beam.use(UsersService.class).login(APIConfig.getAccount().getName(), APIConfig.getAccount().getPassKey()).get();
        } catch (InterruptedException | ExecutionException e) {
            App.logger.catching(e);
        }
    }

    @Override
    public boolean isConnected(String channelName) {
        return beamChannels.containsKey(channelName);
    }

    @Override
    public boolean isChannel(String channelName) {
        try {
            for(BeamUser user : beam.use(UsersService.class).search(channelName).get()){
                if (user.username.equalsIgnoreCase(channelName)){
                   return true;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            App.logger.catching(e);
        }
        return false;
    }

    @Override
    public void shutdown() {
        for(String key : beamChannels.keySet()){
            beamChannels.get(key).beamChatConnectable.close();
        }
        beamChannels.clear();
    }

    @Override
    public String getUserName() {
        return beamUser.username;
    }

    @Override
    public DatabaseWrapper getBotDB() {
        return botDB;
    }

    @Override
    public boolean isUserMod(String channel, String user) {
        return false;
    }

    @Override
    public void sendMessage(String channel, String message) {
        beamChannels.get(channel).beamChatConnectable.send(ChatSendMethod.of(message));
    }

    @Override
    public void startBot() {

    }

    @Override
    public boolean join(String channelName) {
        beamChannels.put(channelName,new BeamChatChannel(channelName));
        return beamChannels.containsKey(channelName);
    }

    @Override
    public boolean leave(String channelName) {
        beamChannels.remove(channelName);
        beamChannels.get(channelName).beamChatConnectable.close();
        return !beamChannels.containsKey(channelName);
    }

}