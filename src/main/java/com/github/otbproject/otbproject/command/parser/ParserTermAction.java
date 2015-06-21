package com.github.otbproject.otbproject.command.parser;

public interface ParserTermAction {
    String apply(String userNick, String channel, int count, String[] args, String term) throws InvalidTermException;
}
