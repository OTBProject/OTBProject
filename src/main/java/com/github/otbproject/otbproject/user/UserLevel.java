package com.github.otbproject.otbproject.user;


public enum UserLevel {
    IGNORED(-10), DEFAULT(0), SUBSCRIBER(5), REGULAR(10), MODERATOR(20), SUPER_MODERATOR(30), BROADCASTER(40), INTERNAL(50), TOO_HIGH(200);

    private final int value;

    UserLevel(int userLevel) {
        this.value = userLevel;
    }

    public int getValue() {
        return value;
    }
}
