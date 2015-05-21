package com.github.otbproject.otbproject.cli.commands;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.api.APIBot;
import com.github.otbproject.otbproject.api.APIChannel;
import com.github.otbproject.otbproject.commands.loader.FSCommandLoader;
import com.github.otbproject.otbproject.commands.loader.LoadingSet;
import com.github.otbproject.otbproject.messages.internal.InternalMessageSender;
import com.github.otbproject.otbproject.messages.receive.PackagedMessage;
import com.github.otbproject.otbproject.messages.send.MessagePriority;
import com.github.otbproject.otbproject.users.UserLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class CmdParser {

    public static final String STOP = "stop";
    public static final String RESTART = "restart";
    public static final String JOINCHANNEL = "join";
    public static final String LEAVECHANNEL = "leave";
    public static final String RELOAD = "reload";
    public static final String EXEC = "exec";
    private static final HashMap<String, Runnable> mapOfThings = new HashMap<>();
    private static final ArrayList<String> args = new ArrayList<>();
    private static String responseStr = "";
    private static String name = "RETURN CHARACTER";//Honestly useless assignment, but something has to be here, why not this?

    static {
        //Java 8 magic
        mapOfThings.put(STOP, CmdParser::stop);
        mapOfThings.put(RESTART, CmdParser::restart);
        mapOfThings.put(JOINCHANNEL, CmdParser::joinChannel);
        mapOfThings.put(LEAVECHANNEL, CmdParser::leaveChannel);
        mapOfThings.put(RELOAD, CmdParser::reload);
        mapOfThings.put(EXEC, CmdParser::exec);
    }

    public static String processLine(String aLine) {
        //use a second Scanner to parse the content of each line

        aLine = aLine.trim();

        Scanner scanner = new Scanner(aLine);
        scanner.useDelimiter(" ");
        try {
            if (scanner.hasNext()) {
                App.logger.debug("Processing input line: " + aLine);
                name = scanner.next().toLowerCase();
                while (scanner.hasNext()) {
                    args.add(scanner.next());
                }
                if (mapOfThings.containsKey(name)) {
                    mapOfThings.get(name).run();
                } else {
                    printHelpNoCommand();
                }
                if (!responseStr.isEmpty()) {
                    App.logger.debug(responseStr);
                }
            } else {
                App.logger.warn("Empty or invalid line. Unable to process.");
            }
            return responseStr;
        } finally {
            scanner.close();
            responseStr = "";
            args.clear();

        }
    }


    static void stop() {
        App.logger.info("Stopping the process");
        if (APIBot.getBot() != null && APIBot.getBot().isConnected()) {
            APIBot.getBot().shutdown();
        }
        App.logger.info("Process Stopped, Goodbye");
        System.exit(0);
    }

    static void restart() {
        if (APIBot.getBot() != null && APIBot.getBot().isConnected()) {
            APIBot.getBot().shutdown();
        }
        FSCommandLoader.LoadCommands();
        FSCommandLoader.LoadAliases();
        try {
            APIBot.setBotThread(new Thread(APIBot.getBotRunnable()));
            APIBot.getBotThread().start();
        } catch (IllegalThreadStateException e) {
            App.logger.catching(e);
        }
    }

    static void joinChannel() {
        if (args.size() > 0) {
            boolean success = APIChannel.join(args.get(0).toLowerCase(), true);
            String string = success ? "Successfully joined" : "Failed to join";
            responseStr = string + " channel: " + args.get(0).toLowerCase();
        } else {
            responseStr = "Not Enough Args for '" + JOINCHANNEL + "'";
        }
    }

    static void leaveChannel() {
        if (args.size() > 0) {
            boolean success = APIChannel.leave(args.get(0).toLowerCase());
            String string = success ? "Successfully left" : "Failed to leave";
            responseStr = string + " channel: " + args.get(0).toLowerCase();
        } else {
            responseStr = "Not Enough Args for '" + LEAVECHANNEL + "'";
        }
    }

    static void reload() {
        if (args.size() > 0) {
            try {
                FSCommandLoader.LoadLoadedCommands(args.get(0).toLowerCase(), LoadingSet.BOTH);
            } catch (IOException e) {
                App.logger.catching(e);
                responseStr += "Reload of Commands for " + args.get(0).toLowerCase() + " failed. \n " + e.getLocalizedMessage() + "\n";
            }
            try {
                FSCommandLoader.LoadLoadedAliases(args.get(0).toLowerCase(), LoadingSet.BOTH);
            } catch (IOException e) {
                App.logger.catching(e);
                responseStr += "Reload of Aliases for " + args.get(0).toLowerCase() + " failed. \n " + e.getLocalizedMessage() + "\n";
            }
        } else {
            FSCommandLoader.LoadLoadedCommands(LoadingSet.BOTH);
            FSCommandLoader.LoadLoadedAliases(LoadingSet.BOTH);
        }
        responseStr += "Reload Complete";
    }

    static void exec() {
        if (args.size() < 2) {
            responseStr = "Not enough args for '" + EXEC + "'";
            return;
        }
        String channelName = args.get(0).toLowerCase();
        if (!APIChannel.in(channelName)) {
            responseStr = "Not in channel: " + channelName;
            return;
        }
        UserLevel ul = UserLevel.INTERNAL;
        String command = args.get(1);
        for (int i = 2; i < args.size(); i++) {
            command += " ";
            command += args.get(i);
        }
        PackagedMessage packagedMessage = new PackagedMessage(command, InternalMessageSender.DESTINATION_PREFIX + InternalMessageSender.CLI, channelName, InternalMessageSender.DESTINATION_PREFIX + InternalMessageSender.CLI, ul, MessagePriority.DEFAULT);
        try {
            APIChannel.get(channelName).receiveMessage(packagedMessage);
        } catch (NullPointerException npe) {
            App.logger.catching(npe);
        }
    }


    static void printHelpNoCommand() {
        responseStr = "That command is invalid. \'" + name + "\' does not exist as a CLI command.";
    }

    void printHelp() {
        App.logger.info("Invalid arguments.");
    }

}
