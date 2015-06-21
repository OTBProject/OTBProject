package com.github.otbproject.otbproject;

import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.cli.ArgParser;
import com.github.otbproject.otbproject.cli.commands.CmdParser;
import com.github.otbproject.otbproject.config.*;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.fs.PathBuilder;
import com.github.otbproject.otbproject.fs.Setup;
import com.github.otbproject.otbproject.fs.groups.Base;
import com.github.otbproject.otbproject.fs.groups.Chan;
import com.github.otbproject.otbproject.fs.groups.Load;
import com.github.otbproject.otbproject.gui.GuiApplication;
import com.github.otbproject.otbproject.util.UnPacker;
import com.github.otbproject.otbproject.util.Util;
import com.github.otbproject.otbproject.util.VersionClass;
import com.github.otbproject.otbproject.util.compat.VersionCompatHelper;
import com.github.otbproject.otbproject.util.preload.LoadStrategy;
import com.github.otbproject.otbproject.web.WarDownload;
import com.github.otbproject.otbproject.web.WebStart;
import com.github.otbproject.otbproject.web.WebVersion;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class App {
    public static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    public static final Logger logger = LogManager.getLogger();
    public static final String VERSION = new VersionClass().getVersion();
    public static final ConfigManager configManager = new ConfigManager();

    public static void main(String[] args) {
        try {
            doMain(args);
        } catch (Throwable t) {
            try {
                System.err.println("A fatal problem has occurred.");
                t.printStackTrace();
                System.err.println("Attempting to log problem.");
                // log throwable
                DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
                Date date = new Date();
                File file = new File("OTBProjectFatal-"+dateFormat.format(date)+".log");
                if  (!file.createNewFile()) {
                    throw new IOException("Failed to create fatal log file for some reason.");
                }
                PrintStream ps = new PrintStream(file);
                t.printStackTrace(ps);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.exit(-10);
            }
        }
    }



    private static void doMain(String[] args) {
        CommandLine cmd = initialArgParse(args);

        System.setProperty("OTBCONF", FSUtil.logsDir());
        org.apache.logging.log4j.core.Logger coreLogger
                = (org.apache.logging.log4j.core.Logger) LogManager.getLogger();
        LoggerContext context
                = coreLogger.getContext();
        org.apache.logging.log4j.core.config.Configuration config
                = context.getConfiguration();

        if (cmd.hasOption(ArgParser.Opts.DEBUG)) {
            coreLogger.removeAppender(config.getAppender("Console-info"));
            coreLogger.removeAppender(config.getAppender("Routing-console-info"));
        } else {
            coreLogger.removeAppender(config.getAppender("Console-debug"));
            coreLogger.removeAppender(config.getAppender("Routing-console-debug"));
        }
        File logFile = new File(FSUtil.logsDir() + File.separator + "console.log");
        logFile.delete();

        // Log version
        logger.info("OTBProject version " + VERSION);
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                App.logger.catching(e);
            }
            Util.getSingleThreadExecutor().execute(() -> GuiApplication.start(args));
        }

        // Ensure directory tree is setup
        try {
            Setup.setup();
        } catch (IOException e) {
            logger.error("Unable to setup main directory tree at:\t" + FSUtil.getBaseDir());
            logger.catching(e);
            System.exit(1);
        }

        File versionFile = new File(FSUtil.configDir() + File.separator + "VERSION");
        try {
            if (!versionFile.exists() && !versionFile.createNewFile()) {
                throw new IOException("Failed to create version file");
            }
        } catch (IOException e) {
            logger.catching(e);
        }
        String version = "";
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(versionFile));
            version = fileReader.readLine();
        } catch (IOException e) {
            logger.catching(e);
        }
        if (cmd.hasOption(ArgParser.Opts.UNPACK) || (!VERSION.contains("SNAPSHOT") && !VERSION.equalsIgnoreCase(version) && !cmd.hasOption(ArgParser.Opts.NO_UNPACK))) {
            if (!VERSION.equalsIgnoreCase(version)) {
                VersionCompatHelper.fixCompatIssues(version);
            }
            PathBuilder builder = new PathBuilder();
            UnPacker.unPack("preloads/json/commands/", builder.base(Base.CMD).channels(Chan.ALL).load(Load.TO).create());
            UnPacker.unPack("preloads/json/aliases/", builder.base(Base.ALIAS).channels(Chan.ALL).load(Load.TO).create());
            UnPacker.unPack("preloads/json/bot-channel/commands/", builder.base(Base.CMD).channels(Chan.BOT).load(Load.TO).create());
            UnPacker.unPack("preloads/groovy/scripts/commands/", FSUtil.commandScriptDir());
            Bot.Control.loadPreloads(LoadStrategy.UPDATE);
        }
        try {
            PrintStream ps = new PrintStream(versionFile);
            ps.println(VERSION);
        } catch (IOException e) {
            logger.catching(e);
        }

        // Perform various startup actions
        Bot.Control.startup(cmd);
/*
        try{
            if(new File(WebStart.WAR_PATH).exists()){
                WebStart.main(args);
            }else if(!VERSION.contains("-SNAPSHOT")){
                Thread thread = new Thread(new WarDownload());
                thread.start();
                thread.join();
                WebStart.main(args);
            }else{
                logger.warn("You are running a dev build of OTBProject, please also grab the latest build of the web interface and place in \""+FSUtil.webDir()+File.separator+"\" as \"web-interface-"+WebVersion.get()+".war\". Releases will automatically download this for you");
            }
        }catch (Exception e){
            logger.catching(e);
        }
        */
        if (!GraphicsEnvironment.isHeadless()) {
            GuiApplication.setInputActive();
        }

        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        while (scanner.hasNext()) {
            String in = scanner.next();
            if (!in.equals(""))
                CmdParser.processLine(in);
        }
        scanner.close();
    }

    private static CommandLine initialArgParse(String[] args) {
        CommandLine cmd = null;
        try {
            cmd = ArgParser.parse(args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            ArgParser.printHelp();
            System.exit(1);
        }

        if (cmd.hasOption(ArgParser.Opts.HELP)) {
            ArgParser.printHelp();
            System.exit(0);
        }

        if (cmd.hasOption(ArgParser.Opts.VERSION)) {
            System.out.println("OTBProject version " + VERSION);
            System.exit(0);
        }

        if (cmd.hasOption(ArgParser.Opts.BASE_DIR)) {
            String path = cmd.getOptionValue(ArgParser.Opts.BASE_DIR);
            if (new File(path).isDirectory()) {
                if (path.endsWith(File.separator)) {
                    path = path.substring(0, path.length() - 1);
                }
                FSUtil.setBaseDirPath(path);
            } else {
                System.out.println("Error setting base directory.");
                System.out.println("The path:\t" + path);
                System.out.println("does not exist or is not a directory.");
                System.out.println();
                ArgParser.printHelp();
                System.exit(2);
            }
        }

        return cmd;
    }

}
