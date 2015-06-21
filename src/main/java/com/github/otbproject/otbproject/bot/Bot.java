package com.github.otbproject.otbproject.bot;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.beam.BeamBot;
import com.github.otbproject.otbproject.bot.irc.IRCBot;
import com.github.otbproject.otbproject.bot.irc.InputParserImproved;
import com.github.otbproject.otbproject.cli.ArgParser;
import com.github.otbproject.otbproject.command.parser.CommandResponseParser;
import com.github.otbproject.otbproject.command.parser.TermLoader;
import com.github.otbproject.otbproject.config.*;
import com.github.otbproject.otbproject.filter.FilterProcessor;
import com.github.otbproject.otbproject.fs.groups.Base;
import com.github.otbproject.otbproject.fs.groups.Chan;
import com.github.otbproject.otbproject.proc.CommandScriptProcessor;
import com.github.otbproject.otbproject.util.LibsLoader;
import com.github.otbproject.otbproject.util.Util;
import com.github.otbproject.otbproject.util.preload.LoadStrategy;
import com.github.otbproject.otbproject.util.preload.PreloadLoader;
import org.apache.commons.cli.CommandLine;

import java.lang.reflect.Field;
import java.util.concurrent.Future;

public class Bot {
    private static IBot bot;
    private static Future<?> botFuture;
    private static Runnable botRunnable;

    public static IBot getBot() {
        return bot;
    }

    public static void setBot(IBot bot) {
        Bot.bot = bot;
    }

    public static Future<?> getBotFuture() {
        return botFuture;
    }

    public static void setBotFuture(Future<?> botFuture) {
        Bot.botFuture = botFuture;
    }

    public static Runnable getBotRunnable() {
        return botRunnable;
    }

    public static void setBotRunnable(Runnable botRunnable) {
        Bot.botRunnable = botRunnable;
    }

    public static class Control {
        private static boolean running = false;

        public static synchronized boolean startup(CommandLine cmd) {
            loadConfigs(cmd);
            LibsLoader.load();
            init();
            running = createBot();
            return running;
        }

        public static synchronized boolean restart() {
            shutdown(true);
            try {
                return startup();
            } catch (StartupException e) {
                App.logger.catching(e);
                App.logger.error("Unknown error restarting bot");
                return false;
            }
        }

        /**
         * Stops the bot and cleans up anything which needs to be cleaned up
         *  before the bot is started again
         *
         * @param cleanup whether or not to cleanup various data with the
         *                expectation that the bot will be started again
         */
        public static synchronized void shutdown(boolean cleanup) {
            IBot bot = getBot();
            if (bot != null) {
                bot.shutdown();
            }
            if (cleanup) {
                shutdownCleanup();
            }
            running = false;
        }

        private static synchronized void shutdownCleanup() {
            clearCaches();
            // TODO unload libs?
        }

        /**
         * Should NOT be used for initial startup. Does not handle command line
         *  args. The function startup(CommandLine) should instead be used to
         *  start the bot the first time.
         *
         * Should be run to start the bot after shutdown() has been called
         *
         * @return true if bot started successfully
         * @throws StartupException if bot is already running
         */
        public static synchronized boolean startup() throws StartupException {
            if (running) {
                throw new StartupException("Unable to start bot: bot already running");
            }

            loadConfigs();
            CommandResponseParser.reRegisterTerms();
            init();
            running = createBot();
            if (!running) {
                shutdown(true);
            }
            return running;
        }

        private static void clearCaches() {
            CommandScriptProcessor.clearScriptCache();
            FilterProcessor.clearScriptCache();
        }

        private static boolean createBot() {
            // Connect to service
            try {
                switch (Configs.getGeneralConfig().getServiceName()){
                    case TWITCH:
                        setBot(new IRCBot());
                        Class c = getBot().getClass().getSuperclass();
                        Field input = c.getDeclaredField("inputParser");
                        input.setAccessible(true);
                        input.set(getBot(), new InputParserImproved((IRCBot) getBot()));
                        break;
                    case BEAM:
                        setBot(new BeamBot());
                        break;
                }
            } catch (BotInitException | NoSuchFieldException | IllegalAccessException e) {
                App.logger.catching(e);
            }
            if (getBot() == null) {
                App.logger.error("Failed to start bot");
                return false;
            } else {
                setBotRunnable(new BotRunnable());
                setBotFuture(Util.getSingleThreadExecutor("Bot").submit(getBotRunnable()));
                return true;
            }
        }

        private static void init() {
            loadPreloads(LoadStrategy.OVERWRITE);
            TermLoader.loadTerms();
            // TODO load filters (scripts)
        }

        public static void loadPreloads(LoadStrategy strategy) {
            // TODO stream over Base when filters are ready
            PreloadLoader.loadDirectory(Base.CMD, Chan.ALL, null, strategy);
            PreloadLoader.loadDirectory(Base.ALIAS, Chan.ALL, null, strategy);
            PreloadLoader.loadDirectory(Base.CMD, Chan.BOT, null, strategy);
            PreloadLoader.loadDirectory(Base.ALIAS, Chan.BOT, null, strategy);

            PreloadLoader.loadDirectoryForEachChannel(Base.CMD, strategy);
            PreloadLoader.loadDirectoryForEachChannel(Base.ALIAS, strategy);
        }

        private static void loadConfigs() {
            // General config
            GeneralConfig generalConfig = Configs.readGeneralConfig(); // Must be read first for service info
            App.configManager.setGeneralConfig(generalConfig);

            // Account config
            Account account = Configs.readAccount();
            App.configManager.setAccount(account);

            // Bot config
            BotConfig botConfig = Configs.readBotConfig();
            App.configManager.setBotConfig(botConfig);
        }

        private static void loadConfigs(CommandLine cmd) {
            // General config
            GeneralConfig generalConfig = Configs.readGeneralConfig(); // Must be read first for service info
            App.configManager.setGeneralConfig(generalConfig);
            if (cmd.hasOption(ArgParser.Opts.SERVICE)) {
                String serviceName = cmd.getOptionValue(ArgParser.Opts.SERVICE).toUpperCase();
                if (serviceName.equals(ServiceName.TWITCH.toString())) {
                    Configs.getGeneralConfig().setServiceName(ServiceName.TWITCH);
                } else if (serviceName.equals(ServiceName.BEAM.toString())) {
                    Configs.getGeneralConfig().setServiceName(ServiceName.BEAM);
                } else {
                    App.logger.error("Invalid service name: " + serviceName);
                    ArgParser.printHelp();
                    System.exit(1);
                }
                Configs.writeGeneralConfig();
            }

            // Account config
            if (cmd.hasOption(ArgParser.Opts.ACCOUNT_FILE)) {
                Configs.setAccountFileName(cmd.getOptionValue(ArgParser.Opts.ACCOUNT_FILE));
            }
            Account account = Configs.readAccount();
            if (cmd.hasOption(ArgParser.Opts.ACCOUNT)) {
                account.setName(cmd.getOptionValue(ArgParser.Opts.ACCOUNT));
            }
            if (cmd.hasOption(ArgParser.Opts.PASSKEY)) {
                account.setPasskey(cmd.getOptionValue(ArgParser.Opts.PASSKEY));
            }
            App.configManager.setAccount(account);
            Configs.writeAccount();

            // Bot config
            BotConfig botConfig = Configs.readBotConfig();
            App.configManager.setBotConfig(botConfig);
        }
    }

    public static class StartupException extends Exception {
        private StartupException() {
            super();
        }

        private StartupException(String message) {
            super(message);
        }
    }
}
