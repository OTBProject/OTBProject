package com.github.otbproject.otbproject.bot.beam;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.channel.ChannelInitException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import net.jodah.expiringmap.ExpiringMap;
import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.channel.BeamChannel;
import pro.beam.api.resource.chat.BeamChat;
import pro.beam.api.resource.chat.BeamChatConnectable;
import pro.beam.api.resource.chat.events.IncomingMessageEvent;
import pro.beam.api.resource.chat.events.UserJoinEvent;
import pro.beam.api.resource.chat.events.UserLeaveEvent;
import pro.beam.api.resource.chat.events.data.IncomingMessageData;
import pro.beam.api.resource.chat.methods.AuthenticateMessage;
import pro.beam.api.services.impl.ChatService;
import pro.beam.api.services.impl.UsersService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeamChatChannel {
    final BeamBot beamBot;
    BeamChat beamChat;
    final BeamChatConnectable beamChatConnectable;
    BeamChannel channel;
    public final ExpiringMap<String, Boolean> timeoutSet;

    public final ConcurrentHashMap<String, List<BeamUser.Role>> userRoles = new ConcurrentHashMap<>();
    private static final int USER_LIST_TIMEOUT = 4;
    private static final int USER_LIST_MAX_SIZE = 100;

    private static final int CACHE_EXPIRATION_MIN = 5;
    private static final int CACHE_MAX_SIZE = 200;
    private final ConcurrentHashMap<String, Set<String>> cacheLookup = new ConcurrentHashMap<>();
    private final Cache<String, IncomingMessageData> messageCache;


    private BeamChatChannel(String channelName) throws ChannelInitException {
        timeoutSet = ExpiringMap.builder()
                .variableExpiration()
                .expirationPolicy(ExpiringMap.ExpirationPolicy.CREATED)
                .build();

        // Initialize cache
        messageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRATION_MIN, TimeUnit.MINUTES)
                .maximumSize(CACHE_MAX_SIZE)
                .removalListener(new RemovalListener<String, IncomingMessageData>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, IncomingMessageData> notification) {
                        IncomingMessageData data = notification.getValue();
                        if (data != null) {
                            removeFromCacheLookup(notification.getValue().user_name.toLowerCase(), notification.getKey());
                        }
                    }
                })
                .build();

        beamBot = ((BeamBot) Bot.getBot());
        try {
            channel = beamBot.beamUser.channel;
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
        beamChatConnectable = beamChat.makeConnectable(beamBot.beam);
        boolean connected;
        try {
            connected = beamChatConnectable.connectBlocking();
        } catch (InterruptedException e) {
            App.logger.catching(e);
            throw new ChannelInitException("Failed to connect to Beam channel: " + channelName);
        }
        if (!connected) {
            throw new ChannelInitException("Failed to connect to Beam channel: " + channelName);
        }

        beamChatConnectable.send(AuthenticateMessage.from(channel, beamBot.beamUser, beamChat.authkey));
        try {
            Thread.sleep(200);// needed to allow the authentication
        } catch (InterruptedException e) {
            App.logger.catching(e);
        }

        // Get list of users and roles
        String path = BeamAPI.BASE_PATH.resolve("chats/" + channel.id + "/users").toString();
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("limit", USER_LIST_MAX_SIZE);
            BeamChatUser[] users = ((BeamBot) Bot.getBot()).beam.http.get(path, BeamChatUser[].class, map).get(USER_LIST_TIMEOUT, TimeUnit.SECONDS);
            Map<String, List<BeamUser.Role>> roleMap =
                    Stream.of(users).collect(Collectors.toMap(user -> user.getUserName().toLowerCase(),
                            user -> Collections.unmodifiableList(user.getUserRoles())));
            userRoles.putAll(roleMap);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            App.logger.catching(e);
        }

        App.logger.info("Connected to: " + channelName);
    }

    private void init(String channelName) {
        beamChatConnectable.on(IncomingMessageEvent.class, new MessageHandler(channelName, this));
        beamChatConnectable.on(UserJoinEvent.class, new UserJoinHandler(this));
        beamChatConnectable.on(UserLeaveEvent.class, new UserLeaveHandler(this));
    }

    public static BeamChatChannel create(String channelName) throws ChannelInitException {
        BeamChatChannel channel = new BeamChatChannel(channelName);
        channel.init(channelName);
        return channel;
    }

    // TODO maybe lock
    private void addToCacheLookup(String user, String messageId) {
        Set<String> set = cacheLookup.get(user);
        if (set != null) {
            set.add(messageId);
        } else {
            Set<String> newSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
            newSet.add(messageId);
            set = cacheLookup.put(user, newSet);
            if (set != null) {
                newSet.addAll(set);
            }
        }
    }

    // TODO maybe lock
    private boolean removeFromCacheLookup(String user, String messageId) {
        Set<String> set = cacheLookup.get(user);
        if (set != null) {
            boolean success = set.remove(messageId);
            if (set.isEmpty()) {
                cacheLookup.remove(user);
            }
            return success;
        }
        return false;
    }

    public void cacheMessage(IncomingMessageData data) {
        if (data == null) {
            return;
        }
        addToCacheLookup(data.user_name.toLowerCase(), data.id);
        messageCache.put(data.id, data);
    }

    public void clearCache() {
        messageCache.invalidateAll();
        cacheLookup.clear();
    }

    public void invalidateAllForUser(String user) {
        Set<String> set = cacheLookup.get(user);
        if (set == null) {
            return;
        }
        set.forEach(messageCache::invalidate);
    }

    public void deleteCachedMessages(String user) {
        Set<String> set = cacheLookup.get(user);
        if (set == null) {
            return;
        }
        set.forEach(id -> {
            IncomingMessageData data = messageCache.asMap().get(id);
            if (data != null) {
                beamChatConnectable.delete(data);
                messageCache.invalidate(id);
            }
        });
    }
}
