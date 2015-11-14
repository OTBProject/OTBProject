package com.github.otbproject.otbproject.bot.beam;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.Control;
import com.github.otbproject.otbproject.channel.ChannelInitException;
import com.github.otbproject.otbproject.util.ThreadUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.Uninterruptibles;
import net.jodah.expiringmap.ExpiringMap;
import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.channel.BeamChannel;
import pro.beam.api.resource.chat.BeamChat;
import pro.beam.api.resource.chat.BeamChatConnectable;
import pro.beam.api.resource.chat.events.DeleteMessageEvent;
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

class BeamChatChannel {
    private static final String ERR_MSG = "Failed to connect to Beam channel";
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
    private final SetMultimap<String, String> cacheLookup = Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
    final Cache<String, IncomingMessageData> messageCache;


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
                            cacheLookup.remove(notification.getValue().userName.toLowerCase(), notification.getKey());
                        }
                    }
                })
                .build();

        beamBot = ((BeamBot) Control.bot());
        try {
            BeamUser beamUser = beamBot.beam.use(UsersService.class).search(channelName).get().stream()
                    .filter(user -> user.username.equalsIgnoreCase(channelName))
                    .findAny().orElseThrow(() -> new ChannelInitException(channelName, ERR_MSG));
            channel = beamBot.beam.use(UsersService.class).findOne(beamUser.id).get().channel;
            beamChat = beamBot.beam.use(ChatService.class).findOne(channel.id).get();
        } catch (InterruptedException | ExecutionException e) {
            ThreadUtil.interruptIfInterruptedException(e);
            throw new ChannelInitException(channelName, ERR_MSG, e);
        }
        beamChatConnectable = beamChat.makeConnectable(beamBot.beam);
        boolean connected;
        try {
            connected = beamChatConnectable.connectBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChannelInitException(channelName, ERR_MSG, e);
        }
        if (!connected) {
            throw new ChannelInitException(channelName, ERR_MSG);
        }

        beamChatConnectable.send(AuthenticateMessage.from(channel, beamBot.beamUser, beamChat.authkey));
        Uninterruptibles.sleepUninterruptibly(200, TimeUnit.MILLISECONDS); // needed to allow the authentication

        // Get list of users and roles
        String path = BeamAPI.BASE_PATH.resolve("chats/" + channel.id + "/users").toString();
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("limit", USER_LIST_MAX_SIZE);
            BeamChatUser[] users = ((BeamBot) Control.bot()).beam.http.get(path, BeamChatUser[].class, map).get(USER_LIST_TIMEOUT, TimeUnit.SECONDS);
            Map<String, List<BeamUser.Role>> roleMap =
                    Stream.of(users).collect(Collectors.toMap(user -> user.getUserName().toLowerCase(),
                            user -> Collections.unmodifiableList(user.getUserRoles())));
            userRoles.putAll(roleMap);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ThreadUtil.interruptIfInterruptedException(e);
            App.logger.catching(e);
        }

        App.logger.info("Connected to: " + channelName);
    }

    private void init(String channelName) {
        beamChatConnectable.on(IncomingMessageEvent.class, new BeamMessageHandler(channelName, this));
        beamChatConnectable.on(UserJoinEvent.class, new UserJoinHandler(this));
        beamChatConnectable.on(UserLeaveEvent.class, new UserLeaveHandler(this));
        beamChatConnectable.on(DeleteMessageEvent.class, new DeletedMessageHandler(this));
    }

    public static BeamChatChannel create(String channelName) throws ChannelInitException {
        BeamChatChannel channel = new BeamChatChannel(channelName);
        channel.init(channelName);
        return channel;
    }

    public void cacheMessage(IncomingMessageData data) {
        if (data == null) {
            return;
        }
        cacheLookup.put(data.userName.toLowerCase(), data.id);
        messageCache.put(data.id, data);
    }

    public void clearCache() {
        messageCache.invalidateAll();
    }

    public void invalidateAllForUser(String user) {
        Set<String> set = cacheLookup.get(user);
        if (set == null) {
            return;
        }
        set.forEach(messageCache::invalidate);
    }

    public void deleteMessages(String user) {
        Set<String> set = cacheLookup.get(user);
        if (set == null) {
            return;
        }
        App.logger.debug("Attempting to delete " + set.size() + " messages from user: " + user);
        set.forEach(id -> {
            IncomingMessageData data = messageCache.asMap().get(id);
            if (data != null) {
                beamChatConnectable.delete(data);
                messageCache.invalidate(id);
            }
        });
    }
}
