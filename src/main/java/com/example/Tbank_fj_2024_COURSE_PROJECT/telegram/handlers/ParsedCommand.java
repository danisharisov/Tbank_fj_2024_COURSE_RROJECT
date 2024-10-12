package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import java.util.List;

public class ParsedCommand {
    private String commandName;
    private List<String> args;

    public ParsedCommand(String commandName, List<String> args) {
        this.commandName = commandName;
        this.args = args;
    }

    public String getCommandName() {
        return commandName;
    }

    public List<String> getArgs() {
        return args;
    }

    public boolean hasArguments() {
        return !args.isEmpty();
    }
}