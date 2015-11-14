package com.github.otbproject.otbproject;

import com.github.otbproject.otbproject.bot.Control;
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
import com.github.otbproject.otbproject.messages.internal.InternalMessageSender;
import com.github.otbproject.otbproject.util.FatalChecker;
import com.github.otbproject.otbproject.util.LogRemover;
import com.github.otbproject.otbproject.util.ThreadUtil;
import com.github.otbproject.otbproject.util.Unpacker;
import com.github.otbproject.otbproject.util.compat.VersionCompatHelper;
import com.github.otbproject.otbproject.util.preload.LoadStrategy;
import com.github.otbproject.otbproject.util.version.AppVersion;
import com.github.otbproject.otbproject.util.version.Version;
import com.github.otbproject.otbproject.util.version.Versions;
import com.github.otbproject.otbproject.web.WebInterface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class App {
    public static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    public static final Logger logger = LogManager.getLogger();
    public static final Version VERSION = AppVersion.current();

    private App() {}

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
                File file = new File("OTBProjectFatal-" + dateFormat.format(date) + ".log");
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create fatal log file for some reason.");
                }
                PrintStream ps = new PrintStream(file);
                t.printStackTrace(ps);
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw t;
        }
    }


    private static void doMain(String[] args) {
        CommandLine cmd = initialArgParse(args);

        System.setProperty("OTBBASE", FSUtil.getBaseDir());
        System.setProperty("OTBCONF", FSUtil.logsDir());

        if (cmd.hasOption(ArgParser.Opts.DEBUG)) {
            System.setProperty("OTBDEBUG", "true");
        } else {
            System.setProperty("OTBDEBUG", "false");
        }
        File logFile = new File(FSUtil.logsDir() + File.separator + "console.log");
        if (logFile.exists() && !logFile.delete()) {
            logger.error("Failed to delete old console log file");
        }

        // Log version
        logger.info("OTBProject version " + VERSION);

        // Ensure directory tree is setup
        try {
            Setup.setup();
        } catch (IOException e) {
            logger.fatal("Unable to setup main directory tree at:\t" + FSUtil.getBaseDir());
            logger.catching(Level.FATAL, e);
            System.exit(1);
        }

        File versionFile = new File(FSUtil.dataDir() + File.separator + "VERSION");
        File oldVersionFile = new File(FSUtil.configDir() + File.separator + "VERSION");
        Version version = Versions.readFromFile(versionFile).orElse(Versions.readFromFile(oldVersionFile).orElse(Version.create(0, 0, 0, Version.Type.RELEASE)));
        Versions.writeToFile(versionFile, VERSION);

        boolean unpack = cmd.hasOption(ArgParser.Opts.UNPACK)
                || ((VERSION.type != Version.Type.SNAPSHOT) && !VERSION.equals(version) && !cmd.hasOption(ArgParser.Opts.NO_UNPACK));

        // Fix urgent compatibility issues (need to be fixed before configs and other things are loaded)
        if (unpack) {
            VersionCompatHelper.urgentCompatFixes(version);
        }

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Control.shutdown(false)));

        // Read configs
        setConfigsFromCmdLineOpts(cmd);

        // Start GUI if applicable
        if (Control.Graphics.present()) {
            ThreadUtil.getSingleThreadExecutor().execute(() -> GuiApplication.start(args));
        }

        // Fix other compatibility issues
        if (unpack) {
            VersionCompatHelper.normalCompatFixes(version);
        }

        // Unpack
        if (unpack) {
            PathBuilder builder = new PathBuilder();
            Unpacker.unpack("preloads/json/commands/", builder.base(Base.CMD).channels(Chan.ALL).load(Load.TO).create());
            Unpacker.unpack("preloads/json/aliases/", builder.base(Base.ALIAS).channels(Chan.ALL).load(Load.TO).create());
            Unpacker.unpack("preloads/json/bot-channel/commands/", builder.base(Base.CMD).channels(Chan.BOT).load(Load.TO).create());
            Unpacker.unpack("preloads/groovy/scripts/", FSUtil.scriptDir());
            Unpacker.unpack("assets/local/", FSUtil.assetsDir());
            Control.loadPreloads(LoadStrategy.UPDATE);
        }

        // Perform various startup actions
        try {
            Control.startup();
        } catch (Control.StartupException e) {
            App.logger.catching(e);
        }

        // Check for previous fatal crash
        FatalChecker.checkForPreviousFatalCrashes();

        // Start web interface
        if (Configs.getWebConfig().get(WebConfig::isEnabled)) {
            WebInterface.start();
            logger.info("Web Interface Started");
        }

        // Allow command input in GUI
        if (Control.Graphics.present()) {
            GuiApplication.setInputActive();
        }

        // Check for new release, if applicable
        if (Configs.getGeneralConfig().get(GeneralConfig::isUpdateChecking)
                && (AppVersion.latest().compareTo(App.VERSION) > 0)
                && (AppVersion.latest().type == Version.Type.RELEASE)) {
            logger.info("New release available: OTB version " + AppVersion.latest());
            logger.info("You can find the new release at: " + "https://github.com/OTBProject/OTBProject/releases/latest");
            if (Control.Graphics.present()) {
                GuiApplication.newReleaseAlert();
            }
        }

        // Delete old logs if applicable
        if (Configs.getGeneralConfig().get(GeneralConfig::getOldLogsRemovedAfter) > 0) {
            LogRemover.removeOldLogs();
        }

        // Start terminal CLI scanner
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        System.out.println("Terminal input is now active.");
        while (scanner.hasNext()) {
            String in = scanner.next();
            if (!in.equals("")) {
                CmdParser.processLineAndThen(in, InternalMessageSender.TERMINAL, System.out::println, () -> {
                });
            }
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
            File pathFile = new File(path);
            if (pathFile.isDirectory()) {
                FSUtil.setBaseDirPath(pathFile.getAbsolutePath());
            } else {
                System.out.println("Error setting base directory.");
                System.out.println("The path:\t" + path);
                System.out.println("does not exist or is not a directory.");
                System.out.println();
                ArgParser.printHelp();
                System.exit(2);
            }
        }

        if (cmd.hasOption(ArgParser.Opts.NO_GUI)) {
            Control.Graphics.useGui(false);
        }

        return cmd;
    }

    public static void setConfigsFromCmdLineOpts(CommandLine cmd) {
        // General config
        if (cmd.hasOption(ArgParser.Opts.SERVICE)) {
            String serviceName = cmd.getOptionValue(ArgParser.Opts.SERVICE).toUpperCase();
            try {
                Configs.getGeneralConfig().edit(config -> config.setService(Service.valueOf(serviceName)));
            } catch (IllegalArgumentException e) {
                logger.fatal("Invalid service name: " + serviceName);
                ArgParser.printHelp();
                System.exit(1);
            }
        }

        // Account config
        if (cmd.hasOption(ArgParser.Opts.ACCOUNT_FILE)) {
            Configs.setAccountFileName(cmd.getOptionValue(ArgParser.Opts.ACCOUNT_FILE));
        }
        Configs.reloadAccount();
        if (cmd.hasOption(ArgParser.Opts.ACCOUNT)) {
            Configs.getAccount().edit(account -> account.setName(cmd.getOptionValue(ArgParser.Opts.ACCOUNT)));
        }
        if (cmd.hasOption(ArgParser.Opts.PASSKEY)) {
            Configs.getAccount().edit(account -> account.setPasskey(cmd.getOptionValue(ArgParser.Opts.PASSKEY)));
        }

        // Web Config
        if (cmd.hasOption(ArgParser.Opts.WEB)) {
            Configs.getWebConfig().edit(webConfig -> webConfig.setEnabled(Boolean.parseBoolean(cmd.getOptionValue(ArgParser.Opts.WEB))));
        }
    }
}
