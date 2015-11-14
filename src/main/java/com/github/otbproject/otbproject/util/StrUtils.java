package com.github.otbproject.otbproject.util;

public class StrUtils {
    private StrUtils() {}

    public static String capitalizeFully(String string) {
        if (string.length() == 0) {
            return string;
        }
        if (string.length() == 1) {
            return string.toUpperCase();
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
