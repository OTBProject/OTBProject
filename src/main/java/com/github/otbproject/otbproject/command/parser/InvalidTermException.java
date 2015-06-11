package com.github.otbproject.otbproject.command.parser;

class InvalidTermException extends Exception {
    public InvalidTermException() {
        super("Invalid term.");
    }

    public InvalidTermException(String s) {
        super(s);
    }
}