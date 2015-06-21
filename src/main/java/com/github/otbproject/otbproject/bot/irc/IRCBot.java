package com.github.otbproject.otbproject.bot.irc;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.BotUtil;
import com.github.otbproject.otbproject.bot.IBot;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.ChannelNotFoundException;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.database.Databases;
import com.github.otbproject.otbproject.serviceapi.ApiRequest;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.output.OutputRaw;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class IRCBot extends PircBotX implements IBot{
    private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();
    private final DatabaseWrapper botDB = Databases.createBotDbWrapper();
    private final OutputRaw newOutputRaw;
    // Should take slightly more than 30 seconds to refill 99 tokens adding 1
    // token every 304 milliseconds
    private final TokenBucket tokenBucket = TokenBuckets.builder().withCapacity(99).withFixedIntervalRefillStrategy(1, 304, TimeUnit.MILLISECONDS).build();

    @SuppressWarnings("unchecked")
    public IRCBot() {
        super(new Configuration.Builder().setName(Configs.getAccount().getName()).setAutoNickChange(false).setCapEnabled(false).addListener(new IrcListener()).setServerHostname("irc.twitch.tv")
                .setServerPort(6667).setServerPassword(Configs.getAccount().getPasskey()).setEncoding(Charset.forName("UTF-8")).buildConfiguration());
        App.logger.info("Bot configuration built");
        newOutputRaw = new OutputRawImproved(this);
    }

    @Override
    public boolean isConnected(String channelName) {
        return userChannelDao.getAllChannels().contains(userChannelDao.getChannel(channelName));
    }

    @Override
    public ConcurrentHashMap<String, Channel> getChannels() {
        return channels;
    }

    @Override
    public boolean isChannel(String channelName) {
        return ApiRequest.attemptRequest("channels/" + channelName, 3, 500) == null;
    }

    @Override
    public synchronized void shutdown() {
        if (isConnected()) {
            super.shutdown(true);
        }
        IBot.super.shutdown();
    }

    @Override
    public String getUserName() {
        return getNick();
    }

    @Override
    public OutputRaw sendRaw() {
        return newOutputRaw;
    }

    /**
     * Overridden because twitch doesnt not follow RFC2812 for numeric code 004, RPL_MYINFO.<br>
     * Twitch sends "-" instead of "&lt;servername&gt; &lt;version&gt; &lt;available user modes&gt;
     * &lt;available channel modes&gt;. <br>
     * As such it generates an IndexOutOfBoundsException when parsing the line for numeric code 004,
     * this override hides the stack trace from the log, for only this instance of the exception.
     */
    @Override
    protected void startLineProcessing() {
        while (true) {
            //Get line from the server
            String line;
            try {
                line = inputReader.readLine();
            } catch (InterruptedIOException iioe) {
                // This will happen if we haven't received anything from the server for a while.
                // So we shall send it a ping to check that we are still connected.
                sendRaw().rawLine("PING " + (System.currentTimeMillis() / 1000));
                // Now we go back to listening for stuff from the server...
                continue;
            } catch (Exception e) {
                if (e instanceof SocketException && getState() == State.DISCONNECTED) {
                    App.logger.info("Shutdown has been called, closing InputParser");
                    return;
                } else {
                    disconnectException = e;
                    //Something is wrong. Assume its bad and begin disconnect
                    App.logger.error("Exception encountered when reading next line from server", e);
                    line = null;
                }
            }

            //End the loop if the line is null
            if (line == null)
                break;

            //Start acting the line
            try {
                inputParser.handleLine(line);
            } catch (IndexOutOfBoundsException e) {
                boolean log = true;
                StackTraceElement[] stackTraceElements = e.getStackTrace();
                for (StackTraceElement stackTraceElement : stackTraceElements) {
                    if (stackTraceElement.getMethodName().equalsIgnoreCase("parse004")) {
                        log = false;
                    }
                }
                if (log) {
                    App.logger.error("Exception encountered when parsing line", e);
                }
            } catch (Exception e) {
                //Exception in client code. Just log and continue
                App.logger.error("Exception encountered when parsing line", e);
            }


            //Do nothing if this thread is being interrupted (meaning shutdown() was run)
            if (Thread.interrupted())
                return;
        }

        //Now that the socket is definatly closed call event, log, and kill the OutputThread
        shutdown();
    }

    public DatabaseWrapper getBotDB() {
        return botDB;
    }

    @Override
    public boolean isUserMod(String channel, String user) {
        return getUserChannelDao().getChannel(getIrcChannelName(channel)).isOp(getUserChannelDao().getUser(user));
    }

    @Override
    public boolean isUserSubscriber(String channelName, String user) {
        Optional<Channel> optional = Channels.get(channelName);
        return optional.isPresent() && optional.get().subscriberStorage.remove(user);
    }

    @Override
    public void sendMessage(String channel, String message) {
        tokenBucket.consume();
        getUserChannelDao().getChannel(getIrcChannelName(channel)).send().message(message);
    }

    @Override
    public boolean leave(String channel) {
        tokenBucket.consume();
        getUserChannelDao().getChannel(getIrcChannelName(channel)).send().part();
        return !getUserChannelDao().getAllChannels().contains(getUserChannelDao().getChannel(channel));
    }

    @Override
    public boolean timeout(String channelName, String user, int timeInSeconds) {
        // Check if user has user level mod or higher
        try {
            if (BotUtil.isModOrHigher(channelName, user)) {
                return false;
            }
        } catch (ChannelNotFoundException e) {
            App.logger.error("Channel '" + channelName + "' did not exist in which to timeout user");
            App.logger.catching(e);
        }

        sendMessage(channelName, ".timeout " + user + " " + timeInSeconds);
        return true;
    }

    @Override
    public boolean removeTimeout(String channelName, String user) {
        sendMessage(channelName, ".unban " + user);
        return true;
    }

    @Override
    public boolean join(String channel) {
        tokenBucket.consume();
        sendIRC().joinChannel(getIrcChannelName(channel));
        return getUserChannelDao().getAllChannels().contains(getUserChannelDao().getChannel(channel));
    }

    public static String getIrcChannelName(String channel) {
        return "#" + channel;
    }

    public static String getInternalChannelName(String channel) {
        return channel.replace("#", "");
    }


    public boolean notLoggedIn(){
        return !loggedIn;
    }
}
