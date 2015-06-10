package com.github.otbproject.otbproject.commands.parser;

import com.github.otbproject.otbproject.api.Bot;
import com.github.otbproject.otbproject.api.Channels;
import com.github.otbproject.otbproject.api.APIConfig;
import com.github.otbproject.otbproject.quotes.Quote;
import com.github.otbproject.otbproject.quotes.Quotes;

import java.util.Arrays;

public class CommandResponseParser {
    private static final String TERM_START = "[[";        // Not a regex
    private static final String TERM_END = "]]";          // Not a regex
    private static final String MODIFIER_DELIM = "\\.";   // regex
    private static final String EMBED_START = "\\{\\{";   // regex
    private static final String EMBED_END = "\\}\\}";     // regex
    private static final String BASE_REGEX_START = "^";
    private static final String BASE_REGEX_END = "(" + MODIFIER_DELIM + "\\w*)?(" + EMBED_START + ".*" + EMBED_END + ")*$";


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
        // [[user.modifier]]
        if (isTerm(term, "user")) {
            return doModifier(userNick, term);
        }
        // [[channel.modifier]]
        else if (isTerm(term, "channel")) {
            return doModifier(channel, term);
        }
        // [[count]] - ignores modifier (because no effect)
        else if (isTerm(term, "count")) {
            return Integer.toString(count);
        }
        // [[quote.modifier]] - can have a modifier, but it's unclear why you want one
        else if (isTerm(term, "quote")) {
            String quoteNumStr = getEmbeddedString(term, 1);
            Quote quote;
            if (quoteNumStr.isEmpty()) {
                quote = Quotes.getRandomQuote(Channels.get(channel).getQuoteDatabaseWrapper());
                if (quote == null) {
                    return "[Error getting random quote]";
                }
            } else {
                try {
                    int quoteNum = Integer.valueOf(quoteNumStr);
                    quote = Quotes.get(Channels.get(channel).getQuoteDatabaseWrapper(), quoteNum);
                    if (quote == null) {
                        return "";
                    }
                } catch (NumberFormatException e) {
                    return "";
                }
            }
            return quote.getText();
        }
        // [[game.modifier]]
        else if (isTerm(term, "game")) {
            return doModifier("a game", term); // TODO fix when able to get game name from twitch
        }
        // [[numargs]] - ignores modifier (because no effect)
        else if (isTerm(term, "numargs")) {
            return Integer.toString(args.length);
        }
        // [[args.modifier{{default}}]]
        else if (isTerm(term, "args")) {
            // If no args, parse default
            if (args.length == 0) {
                return getEmbeddedString(term, 1);
            }
            return doModifier(String.join(" ", args), term);
        }
        // [[ifargs{{string}}]] - ignores modifier
        else if (isTerm(term, "ifargs")) {
            // If no args, return empty string
            if (args.length == 0) {
                return getEmbeddedString(term, 2);
            }
            // Gets conditionally printed string
            return getEmbeddedString(term, 1);
        }
        // [[fromargN.modifier{{default}}]]
        else if (isTerm(term, "fromarg\\d+")) {
            int argNum = getArgNum(term, "fromarg");
            if (args.length < argNum) {
                return getEmbeddedString(term, 1);
            }
            String[] lessArgs = Arrays.copyOfRange(args, (argNum - 1), args.length);
            return doModifier(String.join(" ", lessArgs), term);
        }
        // [[argN.modifier{{default}}]] - N is a natural number
        else if (isTerm(term, "arg\\d+")) {
            int argNum = getArgNum(term, "arg");
            // If insufficient args, parse default
            if (args.length < argNum) {
                return getEmbeddedString(term, 1);
            }
            return doModifier(args[argNum - 1], term);
        }
        // [[ifargN{{string}}]] - N is a natural number; ignores modifier
        else if (isTerm(term, "ifarg\\d+")) {
            int argNum = getArgNum(term, "ifarg");
            // If insufficient args, return empty string
            if (args.length < argNum) {
                return getEmbeddedString(term, 2);
            }
            // Gets conditionally printed string
            return getEmbeddedString(term, 1);
        }
        // [[foreach.modifier{{prepend}}{{append}}]]
        else if (isTerm(term, "foreach")) {
            String prepend = getEmbeddedString(term, 1);
            String append = getEmbeddedString(term, 2);
            StringBuilder result = new StringBuilder();
            String modifier = getModifier(term);

            Arrays.stream(args).forEach(arg -> result.append(prepend).append(modify(arg, modifier)).append(append));
            return result.toString();
        }
        // [[equal{{compare1}}{{compare2}}{{if_same}}{{if_diff}}]]
        else if (isTerm(term, "equal")) {
            String compare1 = getEmbeddedString(term, 1);
            String compare2 = getEmbeddedString(term, 2);
            if (compare1.equals(compare2)) {
                return getEmbeddedString(term, 3);
            }
            return getEmbeddedString(term, 4);
        }
        // [[service]]
        else if (isTerm(term, "service")) {
            return doModifier(ResponseParserUtil.firstCap(APIConfig.getGeneralConfig().getServiceName().toString(), true), term);
        }
        // [[bot]]
        else if (isTerm(term, "bot")) {
            return doModifier(Bot.getBot().getUserName(), term);
        } else {
            throw new InvalidTermException();
        }
    }

    private static boolean isTerm(String possibleTerm, String termName) {
        return possibleTerm.matches(BASE_REGEX_START + termName + BASE_REGEX_END);
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

    private static String doModifier(String toModify, String term) {
        return modify(toModify, getModifier(term));
    }

    private static String modify(String toModify, String modifier) {
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

    private static String getModifier(String word) {
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
    private static String getEmbeddedString(String term, int index) {
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

    private static int getArgNum(String term, String prefix) throws InvalidTermException {
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
