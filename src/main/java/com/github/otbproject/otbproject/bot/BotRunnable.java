package com.github.otbproject.otbproject.bot;

import com.github.otbproject.otbproject.App;
import org.pircbotx.exception.IrcException;

import java.io.IOException;

public class BotRunnable implements Runnable {
    @Override
    public void run() {
        try {
            App.logger.info("Bot Started");
            Bot.getBot().startBot();
            App.logger.info("Bot Stopped");
        } catch (IOException | IrcException e) {
            App.logger.catching(e);
        }
    }
}
