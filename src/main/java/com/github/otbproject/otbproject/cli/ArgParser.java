package com.github.otbproject.otbproject.cli;

import org.apache.commons.cli.*;

public class ArgParser {
    public static CommandLine parse(String[] args) throws ParseException {
        Options options = getMainOptions();

        CommandLineParser parser = new GnuParser();

        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption(ArgParser.Opts.UNPACK) && cmd.hasOption(ArgParser.Opts.NO_UNPACK)) {
            throw new ParseException("Cannot have mutually exclusive options '--" + ArgParser.Opts.UNPACK + "' and '--" + ArgParser.Opts.NO_UNPACK + "'");
        }

        return cmd;
    }

    private static Options getMainOptions() {
        Options options = new Options();

        options.addOption(Opts.HELP_SHORT, Opts.HELP, false, "Prints this help message and exits");

        options.addOption(Opts.VERSION_SHORT, Opts.VERSION, false, "Prints the version and exits");


        // --base-dir=
        OptionBuilder.withLongOpt(Opts.BASE_DIR);
        OptionBuilder.withDescription("The directory in which to find or create a '.otbproject' directory");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("PATH");
        options.addOption(OptionBuilder.create());

        // --account-file=
        OptionBuilder.withLongOpt(Opts.ACCOUNT_FILE);
        OptionBuilder.withDescription("The name of the file in the config directory from which to load account information");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("FILE_NAME");
        options.addOption(OptionBuilder.create());

        // --account=
        OptionBuilder.withLongOpt(Opts.ACCOUNT);
        OptionBuilder.withDescription("The Twitch or Beam account with which to log in. Overrides account name from --" + Opts.ACCOUNT_FILE);
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("ACCT_NAME");
        options.addOption(OptionBuilder.create());

        // --oauth=
        OptionBuilder.withLongOpt(Opts.PASSKEY);
        OptionBuilder.withDescription("The passkey with which to log in. Overrides passkey from --" + Opts.ACCOUNT_FILE);
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("TOKEN");
        options.addOption(OptionBuilder.create());

        // --debug
        OptionBuilder.withLongOpt(Opts.DEBUG);
        OptionBuilder.withDescription("Print debug logs to the console");
        options.addOption(OptionBuilder.create());

        // --unpack
        OptionBuilder.withLongOpt(Opts.UNPACK);
        OptionBuilder.withDescription("Unpacks scripts and preloaded commands and aliases even if it is not a new version");
        options.addOption(OptionBuilder.create());

        // --no-unpack
        OptionBuilder.withLongOpt(Opts.NO_UNPACK);
        OptionBuilder.withDescription("Do not unpack scripts and preloaded commands and aliases even if it is a new version");
        options.addOption(OptionBuilder.create());

        // --service=
        OptionBuilder.withLongOpt(Opts.SERVICE);
        OptionBuilder.withDescription("The name of the chat service to connect to");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("SERVICE_NAME");
        options.addOption(OptionBuilder.create());

        return options;
    }

    public static void printHelp() {
        new HelpFormatter().printHelp("java -jar otbproject-x.x.x.jar [OPTIONS]", ArgParser.getMainOptions());
    }

    public static class Opts {
        public static final String HELP = "help";
        public static final String HELP_SHORT = "h";
        public static final String BASE_DIR = "base-dir";
        public static final String ACCOUNT_FILE = "account-file";
        public static final String ACCOUNT = "account";
        public static final String PASSKEY = "passkey";
        public static final String DEBUG = "debug";
        public static final String UNPACK = "unpack";
        public static final String NO_UNPACK = "no-unpack";
        public static final String SERVICE = "service";
        public static final String VERSION = "version";
        public static final String VERSION_SHORT = "v";
    }
}
