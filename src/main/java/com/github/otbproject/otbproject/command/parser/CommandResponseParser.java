package com.github.otbproject.otbproject.command.parser;

import com.github.otbproject.otbproject.bot.Bot;
import com.github.otbproject.otbproject.channel.Channel;
import com.github.otbproject.otbproject.channel.Channels;
import com.github.otbproject.otbproject.config.Configs;
import com.github.otbproject.otbproject.quote.Quote;
import com.github.otbproject.otbproject.quote.Quotes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class CommandResponseParser {
    private static final String TERM_START = "[[";        // Not a regex
    private static final String TERM_END = "]]";          // Not a regex
    private static final String MODIFIER_DELIM = "\\.";   // regex
    private static final String EMBED_START = "\\{\\{";   // regex
    private static final String EMBED_END = "\\}\\}";     // regex
    private static final String BASE_REGEX_START = "^";
    private static final String BASE_REGEX_END = "(" + MODIFIER_DELIM + "\\w*)?(" + EMBED_START + ".*" + EMBED_END + ")*$";

    private static final Map<Pattern, ParserTermAction> TERMS = new HashMap<>();

    static {
        registerTerms();
    }

    // In case terms need to be reloaded
    public static void reRegisterTerms() {
        TERMS.clear();
        registerTerms();
    }

    private static void registerTerms() {
        // [[user.modifier]]
        registerTerm("user", (userNick, channel, count, args, term) -> doModifier(userNick, term));

        // [[channel.modifier]]
        registerTerm("channel", (userNick, channel, count, args, term) -> doModifier(channel, term));

        // [[count]] - ignores modifier (because no effect)
        registerTerm("count", (userNick, channel, count, args, term) -> Integer.toString(count));

        // [[quote.modifier]] - can have a modifier, but it's unclear why you want one
        registerTerm("quote", (userNick, channel, count, args, term) -> {
            String quoteNumStr = getEmbeddedString(term, 1);
            Quote quote = null;
            Optional<Channel> channelOptional = Channels.get(channel);
            if (quoteNumStr.isEmpty()) {
                if (channelOptional.isPresent()) {
                    quote = Quotes.getRandomQuote(channelOptional.get().getQuoteDatabaseWrapper());
                }
                if (quote == null) {
                    return "[Error getting random quote]";
                }
            } else {
                try {
                    int quoteNum = Integer.valueOf(quoteNumStr);
                    if (channelOptional.isPresent()) {
                        quote = Quotes.get(channelOptional.get().getQuoteDatabaseWrapper(), quoteNum);
                    }
                    if (quote == null) {
                        return "";
                    }
                } catch (NumberFormatException e) {
                    return "";
                }
            }
            return quote.getText();
        });

        // [[game.modifier]]
        // TODO fix when able to get game name from Twitch/Beam
        //registerTerm("game", (userNick, channel, count, args, term) -> doModifier("a game", term));

        // [[numargs]] - ignores modifier (because no effect)
        registerTerm("numargs", (userNick, channel, count, args, term) -> Integer.toString(args.length));

        // [[args.modifier{{default}}]]
        registerTerm("args", (userNick, channel, count, args, term) -> {
            // If no args, parse default
            if (args.length == 0) {
                return getEmbeddedString(term, 1);
            }
            return doModifier(String.join(" ", args), term);
        });

        // [[ifargs{{string}}]] - ignores modifier
        registerTerm("ifargs", (userNick, channel, count, args, term) -> {
            // If no args, return empty string
            if (args.length == 0) {
                return getEmbeddedString(term, 2);
            }
            // Gets conditionally printed string
            return getEmbeddedString(term, 1);
        });

        // [[fromargN.modifier{{default}}]]
        registerTerm("fromarg\\d+", (userNick, channel, count, args, term) -> {
            int argNum = getArgNum(term, "fromarg");
            if (args.length < argNum) {
                return getEmbeddedString(term, 1);
            }
            String[] lessArgs = Arrays.copyOfRange(args, (argNum - 1), args.length);
            return doModifier(String.join(" ", lessArgs), term);
        });

        // [[argN.modifier{{default}}]] - N is a natural number
        registerTerm("arg\\d+", (userNick, channel, count, args, term) -> {
            int argNum = getArgNum(term, "arg");
            // If insufficient args, parse default
            if (args.length < argNum) {
                return getEmbeddedString(term, 1);
            }
            return doModifier(args[argNum - 1], term);
        });

        // [[ifargN{{string}}]] - N is a natural number; ignores modifier
        registerTerm("ifarg\\d+", (userNick, channel, count, args, term) -> {
            int argNum = getArgNum(term, "ifarg");
            // If insufficient args, return empty string
            if (args.length < argNum) {
                return getEmbeddedString(term, 2);
            }
            // Gets conditionally printed string
            return getEmbeddedString(term, 1);
        });

        // [[foreach.modifier{{prepend}}{{append}}]]
        registerTerm("foreach", (userNick, channel, count, args, term) -> {
            String prepend = getEmbeddedString(term, 1);
            String append = getEmbeddedString(term, 2);
            StringBuilder result = new StringBuilder();
            String modifier = getModifier(term);

            Arrays.stream(args).forEach(arg -> result.append(prepend).append(modify(arg, modifier)).append(append));
            return result.toString();
        });

        // [[equal{{compare1}}{{compare2}}{{if_same}}{{if_diff}}]]
        registerTerm("equal", (userNick, channel, count, args, term) -> {
            String compare1 = getEmbeddedString(term, 1);
            String compare2 = getEmbeddedString(term, 2);
            if (compare1.equals(compare2)) {
                return getEmbeddedString(term, 3);
            }
            return getEmbeddedString(term, 4);
        });

        // [[service]]
        registerTerm("service", (userNick, channel, count, args, term) ->
                doModifier(ResponseParserUtil.firstCap(Configs.getGeneralConfig().getServiceName().toString(), true), term));

        // [[bot]]
        registerTerm("bot", (userNick, channel, count, args, term) -> doModifier(Bot.getBot().getUserName(), term));
    }

    public static String parse(String userNick, String channel, int count, String[] args, String rawResponse) {
        return postUnProcessor(parseMessage(userNick, channel, count, args, rawResponse));
    }

    private static String parseMessage(String userNick, String channel, int count, String[] args, String rawResponse) {
        int innerStartIndex;
        int innerEndIndex;
        String temp;

        while (true) {
            // If no more terms, return
            innerStartIndex = rawResponse.lastIndexOf(TERM_START);
            if (innerStartIndex == -1) {
                return rawResponse;
            }
            // If unbalanced term delimiters, return
            innerEndIndex = rawResponse.indexOf(TERM_END, innerStartIndex);
            if (innerEndIndex == -1) {
                return rawResponse;
            }

            // Check if valid term, and replace if so
            try {
                temp = postProcessor(parseTerm(userNick, channel, count, args, rawResponse.substring(innerStartIndex + 2, innerEndIndex)));
            } catch (InvalidTermException e) {
                return rawResponse;
            }
            rawResponse = rawResponse.substring(0, innerStartIndex) + temp + rawResponse.substring(innerEndIndex + 2);
        }
    }

    private static String parseTerm(String userNick, String channel, int count, String[] args, String term) throws InvalidTermException {
        Optional<ParserTermAction> actionOptional = TERMS.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(term).matches())
                .map(Map.Entry::getValue)
                .findAny();
        if (actionOptional.isPresent()) {
            String parsed = actionOptional.get().apply(userNick, channel, count, args, term);
            if (parsed == null) {
                throw new InvalidTermException();
            }
            return parsed;
        } else {
            throw new InvalidTermException();
        }
    }

    static boolean registerTerm(ParserTerm term) {
        return registerTerm(term.value(), term.action());
    }

    private static boolean registerTerm(String value, ParserTermAction action) {
        Pattern pattern = Pattern.compile(BASE_REGEX_START + value + BASE_REGEX_END);
        if (TERMS.containsKey(pattern)) {
            return false;
        }
        TERMS.put(pattern, action);
        return true;
    }

    // Prevents terms and embedded strings from being passed into the parser as
    // arguments to a command (this could lead to infinite looping if, for
    // example, [[args]] is passed in)
    // Regex and strings hard-coded
    private static String postProcessor(String parsedTerm) {
        parsedTerm = parsedTerm.replaceAll("\\[", "\t[");
        parsedTerm = parsedTerm.replaceAll("\\]", "]\t");
        parsedTerm = parsedTerm.replaceAll("\\{", "\t{");
        parsedTerm = parsedTerm.replaceAll("\\}", "}\t");

        return parsedTerm;
    }

    // Removes characters added by postProcessor immediately before returning
    // fully processed message
    private static String postUnProcessor(String message) {
        return message.replaceAll("\t", "");
    }

    static String doModifier(String toModify, String term) {
        return modify(toModify, getModifier(term));
    }

    static String modify(String toModify, String modifier) {
        switch (modifier) {
            case ModifierTypes.LOWER:
                return toModify.toLowerCase();
            case ModifierTypes.UPPER:
                return toModify.toUpperCase();
            case ModifierTypes.FIRST_CAP:
                return ResponseParserUtil.firstCap(toModify, true);
            case ModifierTypes.WORD_CAP:
                return ResponseParserUtil.wordCap(toModify, true);
            case ModifierTypes.FIRST_CAP_SOFT:
                return ResponseParserUtil.firstCap(toModify, false);
            case ModifierTypes.WORD_CAP_SOFT:
                return ResponseParserUtil.wordCap(toModify, false);
            default:
                return toModify;
        }
    }

    static String getModifier(String word) {
        // Split away default arg, if exists
        String[] temp = word.split(EMBED_START);

        // Check if modifier exists; return empty string if not
        temp = temp[0].split(MODIFIER_DELIM);
        if (temp.length == 1) {
            return "";
        }
        return temp[1];
    }

    // Check if given index embedded string exists; return empty string if not
    static String getEmbeddedString(String term, int index) {
        String[] temp = term.split(EMBED_START, 2);
        if (temp.length == 1) {
            return "";
        }

        for (int i = 1; i < index; i++) {
            temp = temp[1].split(EMBED_START, 2);
            if (temp.length == 1) {
                return "";
            }
        }

        // Handle empty embedded string
        if (temp[1].matches("^" + EMBED_END)) {
            return "";
        }
        return temp[1].split(EMBED_END)[0];
    }

    static int getArgNum(String term, String prefix) throws InvalidTermException {
        // Gets arg number
        String argNumStr = term.replaceFirst(prefix, "").split(EMBED_START, 2)[0].split(MODIFIER_DELIM, 2)[0];
        int argNum;
        try {
            argNum = Integer.parseInt(argNumStr);
        } catch (NumberFormatException e) {
            throw new InvalidTermException("'" + argNumStr + "' is not a valid number and cannot be parsed.");
        }

        // If argNum is 0 (or max int overflow)-, invalid term
        if (argNum <= 0) {
            throw new InvalidTermException("Value must be positive.");
        }

        return argNum;
    }
}
