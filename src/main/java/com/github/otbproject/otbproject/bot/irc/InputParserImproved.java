package com.github.otbproject.otbproject.bot.irc;

import com.github.otbproject.otbproject.App;
import org.pircbotx.InputParser;
import org.pircbotx.Utils;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.pircbotx.hooks.events.UnknownEvent;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class InputParserImproved extends InputParser {

    public InputParserImproved(IRCBot bot) {
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
            configuration.getListenerManager().dispatchEvent(new ServerPingEvent<>(bot, parsedLine.get(0)));
            return;
        } else if (command.startsWith("ERROR")) {
            //Server is shutting us down
            ((IRCBot) bot).shutdown();
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
                    if (((IRCBot) bot).notLoggedIn())
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
            configuration.getListenerManager().dispatchEvent(new UnknownEvent<>(bot, line));

            if (((IRCBot) bot).notLoggedIn())
                //Pass to CapHandlers, could be important
                capHandlersFinished.addAll(configuration.getCapHandlers().stream().filter(curCapHandler -> curCapHandler.handleUnknown(bot, line)).collect(Collectors.toList()));
            // Return from the method;
            return;
        }

        if (sourceNick.startsWith(":"))
            sourceNick = sourceNick.substring(1);

        if (((IRCBot) bot).notLoggedIn())
            processConnect(line, command, target, parsedLine);
        processCommand(target, sourceNick, sourceLogin, sourceHostname, command, line, parsedLine);
    }
}
