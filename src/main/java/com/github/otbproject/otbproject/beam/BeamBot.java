package com.github.otbproject.otbproject.beam;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.api.APIChannel;
import com.github.otbproject.otbproject.api.APIConfig;
import com.github.otbproject.otbproject.api.APIDatabase;
import com.github.otbproject.otbproject.bot.IBot;
import com.github.otbproject.otbproject.channels.Channel;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.proc.CooldownSet;
import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.chat.methods.ChatSendMethod;
import pro.beam.api.services.impl.UsersService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by Justin on 05/04/2015.
 */
public class BeamBot implements IBot {
    private HashMap<String, Channel> channels = new HashMap<>();
    private final DatabaseWrapper botDB = APIDatabase.getBotDatabase();

    public final CooldownSet sentMessageCache = new CooldownSet();
    private static final int CACHE_TIME = 4;

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
    public boolean isConnected() {
        return true;
    }

    @Override
    public HashMap<String, Channel> getChannels() {
        return channels;
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
        return beamUser.username.toLowerCase();
    }

    @Override
    public DatabaseWrapper getBotDB() {
        return botDB;
    }

    @Override
    public boolean isUserMod(String channel, String user) {
        String path = BeamAPI.BASE_PATH.resolve("chats/"+beamChannels.get(getUserName()).channel.id+"/users").toString();
        try {
            BeamChatUser[] users = beam.http.get(path,BeamChatUser[].class , new HashMap<>()).get();
            for (BeamChatUser beamChatUser : users) {
                if(beamChatUser.getUser_name().equalsIgnoreCase(user)){
                    return Arrays.asList(beamChatUser.getUser_roles()).contains("Mod");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            App.logger.catching(e);
            return false;
        }
        return false;
    }

    @Override
    public void sendMessage(String channel, String message) {
        beamChannels.get(channel).beamChatConnectable.send(ChatSendMethod.of(message));
        sentMessageCache.add(message, CACHE_TIME);
    }

    @Override
    public void startBot() {
        APIChannel.join(getUserName(),false);
        for (String channelName : APIConfig.getBotConfig().currentChannels) {
            APIChannel.join(channelName, false);
        }
        while(!beamChannels.isEmpty()){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
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
