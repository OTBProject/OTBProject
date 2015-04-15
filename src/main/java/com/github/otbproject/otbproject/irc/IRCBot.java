package com.github.otbproject.otbproject.irc;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.IBot;
import com.github.otbproject.otbproject.api.APIConfig;
import com.github.otbproject.otbproject.api.APIDatabase;
import com.github.otbproject.otbproject.channels.Channel;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.serviceapi.ApiRequest;
import com.github.otbproject.otbproject.util.OutputRawImproved;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.output.OutputRaw;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by justin on 05/02/2015.
 */
public class IRCBot extends PircBotX implements IBot{
    private HashMap<String, Channel> channels = new HashMap<>();
    private final DatabaseWrapper botDB = APIDatabase.getBotDatabase();
    private final OutputRaw newOutputRaw;

    @SuppressWarnings("unchecked")
    public IRCBot() {
        super(new Configuration.Builder().setName(APIConfig.getAccount().getName()).setAutoNickChange(false).setCapEnabled(false).addListener(new IrcListener()).setServerHostname("irc.twitch.tv")
                .setServerPort(6667).setServerPassword(APIConfig.getAccount().getPasskey()).setEncoding(Charset.forName("UTF-8")).buildConfiguration());
        App.logger.info("Bot configuration built");
        newOutputRaw = new OutputRawImproved(this);
    }

    @Override
    public boolean isConnected(String channelName) {
        return userChannelDao.getAllChannels().contains(userChannelDao.getChannel(channelName));
    }

    @Override
    public HashMap<String, Channel> getChannels() {
        return channels;
    }

    @Override
    public boolean isChannel(String channelName) {
        return ApiRequest.attemptRequest("channels/" + channelName, 3, 500) == null;
    }

    @Override
    public void shutdown() {
        super.shutdown(true);
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
    public void sendMessage(String channel, String message) {
        getUserChannelDao().getChannel(getIrcChannelName(channel)).send().message(message);
    }

    @Override
    public boolean leave(String channel) {
        getUserChannelDao().getChannel(getIrcChannelName(channel)).send().part();
        return !getUserChannelDao().getAllChannels().contains(getUserChannelDao().getChannel(channel));
    }

    @Override
    public boolean join(String channel) {
        sendIRC().joinChannel(getIrcChannelName(channel));
        return getUserChannelDao().getAllChannels().contains(getUserChannelDao().getChannel(channel));
    }

    public static String getIrcChannelName(String channel) {
        return "#" + channel;
    }

    public static String getInternalChannelName(String channel) {
        return channel.replace("#", "");
    }


    public boolean isLoggedIn(){
        return loggedIn;
    }
}
