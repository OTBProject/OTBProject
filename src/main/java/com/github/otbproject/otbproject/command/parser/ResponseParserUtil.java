package com.github.otbproject.otbproject.command.parser;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResponseParserUtil {
    public static String firstCap(String string, boolean forceLower) {
        if (string.length() == 0) {
            return string;
        }
        if (string.length() == 1) {
            return string.toUpperCase();
        }

        if (forceLower) {
            return (string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase());
        }
        return (string.substring(0, 1).toUpperCase() + string.substring(1));
    }

    public static String wordCap(String string, boolean forceLower) {
        return Stream.of(string.split(" ")).map(word -> firstCap(word, forceLower)).collect(Collectors.joining(" "));
    }
}
