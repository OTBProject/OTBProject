package com.github.otbproject.otbproject.cli.commands;

import com.github.otbproject.otbproject.App;

import java.util.concurrent.Callable;

public class CliCommand implements Callable<String> {
    private String shortHelp;
    private String longHelp;
    private Callable<String> callable;

    private CliCommand(String shortHelp, String longHelp, Callable<String> callable) {
        this.shortHelp = shortHelp;
        this.longHelp = longHelp;
        this.callable = callable;
    }

    public String call() throws Exception {
        return callable.call();
    }

    public String getLongHelp() {
        return longHelp;
    }

    public String getShortHelp() {
        return shortHelp;
    }

    public String getFullHelp() {
        return shortHelp + "\n" + longHelp;
    }

    public static class Builder {

        private String shortHelp;
        private String longHelp;
        private Callable<String> callable;

        public Builder() {
            init();
        }

        private void init() {
            shortHelp = "No short help message provided";
            longHelp = "No long help message provided";
            callable = () -> {
                App.logger.warn("Missing callable for cli command");
                return "";
            };
        }

        public Builder withShortHelp(String shortHelp) {
            this.shortHelp = shortHelp;
            return this;
        }

        public Builder withLongHelp(String longHelp) {
            this.longHelp = longHelp;
            return this;
        }

        public Builder withAction(Callable<String> callable) {
            this.callable = callable;
            return this;
        }

        public CliCommand create() {
            CliCommand cliCommand = new CliCommand(shortHelp, longHelp, callable);
            init();
            return cliCommand;
        }
    }
}
