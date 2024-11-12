package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.parser;

import java.util.List;

public class ParsedCommand {
    private final String commandName; // Название команды, например "/login"
    private final List<String> args;  // Аргументы команды, например ["username", "password"]

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

    @Override
    public String toString() {
        return "ParsedCommand{" +
                "commandName='" + commandName + '\'' +
                ", args=" + args +
                '}';
    }
}