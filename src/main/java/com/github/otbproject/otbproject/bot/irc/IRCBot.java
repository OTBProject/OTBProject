package com.github.otbproject.otbproject.bot.irc;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.config.Configs;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.output.OutputRaw;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.charset.Charset;

class IRCBot extends PircBotX {
    private final OutputRaw newOutputRaw;

    public IRCBot() {
        super(new Configuration.Builder<>().setName(Configs.getAccount().getName()).setAutoNickChange(false).setCapEnabled(false).addListener(new IrcListener()).setServerHostname("irc.twitch.tv")
                .setServerPort(6667).setServerPassword(Configs.getAccount().getPasskey()).setEncoding(Charset.forName("UTF-8")).buildConfiguration());
        App.logger.info("Bot configuration built");
        newOutputRaw = new OutputRawImproved(this);
    }

    @Override
    public synchronized void shutdown() {
        if (isConnected()) {
            super.shutdown(true);
        }
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

    public boolean notLoggedIn() {
        return !loggedIn;
    }
}
