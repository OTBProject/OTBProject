package com.github.otbproject.otbproject;

import com.github.otbproject.otbproject.channels.Channel;
import com.github.otbproject.otbproject.cli.ArgParser;
import com.github.otbproject.otbproject.config.Account;
import com.github.otbproject.otbproject.config.BotConfig;
import com.github.otbproject.otbproject.config.ConfigValidator;
import com.github.otbproject.otbproject.eventlistener.IrcListener;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.util.DefaultConfigGenerator;
import com.github.otbproject.otbproject.util.JsonHandler;
import com.github.otbproject.otbproject.util.dev.DevHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import java.util.HashSet;

/**
 * Created by justin on 02/01/2015.
 */
public class App {
    public static HashSet<String> channels = new HashSet<>();
    static Listener listener = new IrcListener();
    public static CustomBot bot;
    public static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        CommandLine cmd = null;
        try {
            cmd = ArgParser.parse(args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            ArgParser.printHelp();
            return;
        }

        if ((cmd == null) || cmd.hasOption(ArgParser.Opts.HELP)) {
            ArgParser.printHelp();
            return;
        }

        if (cmd.hasOption(ArgParser.Opts.BASE_DIR)) {
            String path = cmd.getOptionValue(ArgParser.Opts.BASE_DIR);
            if (new File(path).isDirectory()) {
                if (path.endsWith(File.separator)) {
                    path = path.substring(0, path.length() -1);
                }
                FSUtil.setBaseDirPath(path);
            }
            else {
                System.out.println("Error setting base directory.");
                System.out.println("The path:\t" + path);
                System.out.println("does not exist or is not a directory.");
                System.out.println();
                ArgParser.printHelp();
                return;
            }
        }

        System.setProperty("OTBCONF", FSUtil.logsDir());

        // TODO remove before release
        DevHelper.run(args);

        Account account = JsonHandler.readValue(FSUtil.configDir()+ File.separator+"account.json", Account.class);
        account = ConfigValidator.validateAccount(account);
        
        channels = new HashSet<>(JsonHandler.readValue(FSUtil.dataDir()+ File.separator+FSUtil.DirNames.BOT_CHANNEL+ File.separator+"bot-config.json", BotConfig.class).currentChannels);
        //TODO get botname and oauth from config asell as possible server address and port
        Configuration.Builder configurationBuilder = new Configuration.Builder().setName(account.getName()).setAutoNickChange(false).setCapEnabled(false).addListener(listener).setServerHostname("irc.twitch.tv")
                .setServerPort(6667).setServerPassword(account.getOauth()).setEncoding(Charset.forName("UTF-8"));
        for (String channel : channels) {
            configurationBuilder.addAutoJoinChannel("#" + channel);
        }
        Configuration configuration = configurationBuilder.buildConfiguration();

        logger.info("Bot configuration built");
        bot = new CustomBot(configuration);
        for (String channel :channels){
            Channel channel1 = new Channel(channel);
            channel1.join();
            App.bot.channels.put(channel1.getName(),channel1);
        }
        try {
            logger.info("Bot Started");
            bot.startBot();
        } catch (IOException e) {
            logger.catching(e);
        } catch (IrcException e) {
            logger.catching(e);
        }
    }
}
