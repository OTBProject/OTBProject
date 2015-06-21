package com.github.otbproject.otbproject.proc;

public class ProcessedCommand {
    public final String response;
    public final String commandName;
    public final boolean isScript;
    public final String[] args;

    public ProcessedCommand(String response, String commandName, boolean isScript, String[] args) {
        this.response = response;
        this.commandName = commandName;
        this.isScript = isScript;
        this.args = args;
    }
}
