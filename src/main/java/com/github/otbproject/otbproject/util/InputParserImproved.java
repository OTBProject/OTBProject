package com.github.otbproject.otbproject.util;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.CustomBot;
import org.pircbotx.InputParser;
import org.pircbotx.PircBotX;
import org.pircbotx.Utils;
import org.pircbotx.cap.CapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.pircbotx.hooks.events.UnknownEvent;

import java.io.IOException;
import java.util.List;

/**
 * Created by Justin on 29/03/2015.
 */
public class InputParserImproved extends InputParser {

    public InputParserImproved(CustomBot bot) {
        super(bot);
    }

    /**
     * This method handles events when any line of text arrives from the server,
     * then dispatching the appropriate event.
     *
     * @param line The raw line of text from the server.
     */
    public void handleLine(String line) throws IOException, IrcException {

        List<String> parsedLine = Utils.tokenizeLine(line);

        String senderInfo = "";
        if (parsedLine.get(0).charAt(0) == ':')
            senderInfo = parsedLine.remove(0);

        String command = parsedLine.remove(0).toUpperCase(configuration.getLocale());
        if (!command.equals("PING")) {
            App.logger.info(line);
        }
        // Check for server pings.
        if (command.equals("PING")) {
            // Respond to the ping and return immediately.
            configuration.getListenerManager().dispatchEvent(new ServerPingEvent<PircBotX>(bot, parsedLine.get(0)));
            return;
        } else if (command.startsWith("ERROR")) {
            //Server is shutting us down
            ((CustomBot) bot).shutdown();
            return;
        }

        String sourceNick;
        String sourceLogin = "";
        String sourceHostname = "";
        String target = !parsedLine.isEmpty() ? parsedLine.get(0) : "";

        if (target.startsWith(":"))
            target = target.substring(1);

        int exclamation = senderInfo.indexOf('!');
        int at = senderInfo.indexOf('@');
        if (senderInfo.startsWith(":"))
            if (exclamation > 0 && at > 0 && exclamation < at) {
                sourceNick = senderInfo.substring(1, exclamation);
                sourceLogin = senderInfo.substring(exclamation + 1, at);
                sourceHostname = senderInfo.substring(at + 1);
            } else {
                int code = Utils.tryParseInt(command, -1);
                if (code != -1) {
                    if (!((CustomBot) bot).isLoggedIn())
                        processConnect(line, command, target, parsedLine);
                    processServerResponse(code, line, parsedLine);
                    // Return from the method.
                    return;
                } else
                    // This is not a server response.
                    // It must be a nick without login and hostname.
                    // (or maybe a NOTICE or suchlike from the server)
                    //WARNING: Changed from origional PircBot. Instead of command as target, use channel/user (setup later)
                    sourceNick = senderInfo;
            }
        else {
            // We don't know what this line means.
            configuration.getListenerManager().dispatchEvent(new UnknownEvent<PircBotX>(bot, line));

            if (!((CustomBot) bot).isLoggedIn())
                //Pass to CapHandlers, could be important
                for (CapHandler curCapHandler : configuration.getCapHandlers())
                    if (curCapHandler.handleUnknown(bot, line))
                        capHandlersFinished.add(curCapHandler);
            // Return from the method;
            return;
        }

        if (sourceNick.startsWith(":"))
            sourceNick = sourceNick.substring(1);

        if (!((CustomBot) bot).isLoggedIn())
            processConnect(line, command, target, parsedLine);
        processCommand(target, sourceNick, sourceLogin, sourceHostname, command, line, parsedLine);
    }
}
