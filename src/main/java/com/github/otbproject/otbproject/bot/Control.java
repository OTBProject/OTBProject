package com.github.otbproject.otbproject.bot;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.bot.beam.BeamBot;
import com.github.otbproject.otbproject.bot.irc.TwitchBot;
import com.github.otbproject.otbproject.bot.nullbot.NullBot;
import com.github.otbproject.otbproject.command.parser.CommandResponseParser;
import com.github.otbproject.otbproject.command.parser.TermLoader;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.config.GeneralConfig;
import com.github.otbproject.otbproject.config.Service;
import com.github.otbproject.otbproject.filter.FilterProcessor;
import com.github.otbproject.otbproject.fs.groups.Base;
import com.github.otbproject.otbproject.fs.groups.Chan;
import com.github.otbproject.otbproject.proc.CommandScriptProcessor;
import com.github.otbproject.otbproject.util.LibsLoader;
import com.github.otbproject.otbproject.util.ThreadUtil;
import com.github.otbproject.otbproject.util.preload.LoadStrategy;
import com.github.otbproject.otbproject.util.preload.PreloadLoader;

import java.awt.*;

public class Control {
    private static volatile boolean firstStart = true;
    private static volatile boolean running = false;
    private static volatile Bot bot = NullBot.INSTANCE;

    private Control() {}

    public static Bot bot() {
        return bot;
    }

    /**
     * Sets the bot to the given value. Fails if bot is running.
     *
     * @param bot A {@link Bot} to set as the bot
     * @return {@code true} if successfully set new value for bot
     */
    public static synchronized boolean setBot(Bot bot) {
        if (running) {
            return false;
        }
        Control.bot = bot;
        return true;
    }

    /**
     * Restarts the bot. Works whether or not bot is currently running.
     *
     * @return {@code true} if bot started successfully, {@code false} if it failed to start
     */
    public static synchronized boolean restart() {
        shutdown(true);
        try {
            return startup();
        } catch (StartupException e) {
            App.logger.catching(e);
            return false;
        }
    }

    /**
     * Does what it says on the tin
     */
    public static void shutdownAndExit() {
        shutdown(false);
        App.logger.info("Process stopped");
        System.exit(0);
    }

    /**
     * Stops the bot and cleans up anything which needs to be cleaned up
     * before the bot is started again
     *
     * @param cleanup whether or not to cleanup various data with the
     *                expectation that the bot will be started again
     */
    public static synchronized boolean shutdown(boolean cleanup) {
        if (!running) {
            return false;
        }

        App.logger.info("Shutting down bot");
        Bot oldBot = bot;
        bot = NullBot.INSTANCE; // Quickly make calls to getBot() return NullBot
        oldBot.shutdown();
        if (cleanup) {
            shutdownCleanup(oldBot);
        }
        running = false;
        return true;
    }

    private static void shutdownCleanup(Bot bot) {
        clearCaches();
        // TODO unload libs?
    }

    /**
     * Starts the bot
     *
     * @return true if bot started successfully, false if bot was already running
     * @throws StartupException if failed to create bot properly
     */
    public static synchronized boolean startup() throws StartupException {
        if (running) {
            return false;
        }

        loadConfigs();

        if (firstStart) {
            firstStart = false;
            LibsLoader.load();
        } else {
            CommandResponseParser.reRegisterTerms();
        }

        init();
        try {
            createBot();
            running = true;
        } catch (BotInitException e) {
            throw new StartupException("Failed to start bot", e);
        }
        return true;
    }

    private static void clearCaches() {
        CommandScriptProcessor.clearScriptCache();
        FilterProcessor.clearScriptCache();
    }

    private static void createBot() throws BotInitException {
        // Connect to service
        Service service = Configs.getGeneralConfig().getExactlyAsOptional(GeneralConfig::getService)
                .orElseThrow(() -> new BotInitException("Failed to get service"));
        switch (service) {
            case TWITCH:
                bot = new TwitchBot();
                break;
            case BEAM:
                bot = new BeamBot();
                break;
            default:
                throw new BotInitException("Unknown service: " + service);
        }
        ThreadUtil.getSingleThreadExecutor("Bot").execute(() -> {
            try {
                App.logger.info("Bot Started");
                bot.startBot();
                App.logger.info("Bot Stopped");
            } catch (BotInitException e) {
                App.logger.catching(e);
            }
        });
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
        Configs.getGeneralConfig().updateAndAwait();
        Configs.reloadAccount();
    }

    public static class Graphics {
        private static boolean withGui = true;

        public static void useGui(boolean useGui) {
            withGui = useGui;
        }

        public static boolean present() {
            return withGui && !GraphicsEnvironment.isHeadless();
        }
    }

    public static class StartupException extends Exception {
        private StartupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
