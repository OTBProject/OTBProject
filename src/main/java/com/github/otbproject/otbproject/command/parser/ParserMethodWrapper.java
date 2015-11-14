package com.github.otbproject.otbproject.command.parser;

public class ParserMethodWrapper {
    private ParserMethodWrapper() {}

    public static String doModifier(String toModify, String term) {
        return CommandResponseParser.doModifier(toModify, term);
    }

    public static String modify(String toModify, String modifier) {
        return CommandResponseParser.modify(toModify, modifier);
    }

    public static String getModifier(String word) {
        return CommandResponseParser.getModifier(word);
    }

    public static String getEmbeddedString(String term, int index) {
        return CommandResponseParser.getEmbeddedString(term, index);
    }

    public static int getArgNum(String term, String prefix) throws InvalidTermException {
        return CommandResponseParser.getArgNum(term, prefix);
    }
}
