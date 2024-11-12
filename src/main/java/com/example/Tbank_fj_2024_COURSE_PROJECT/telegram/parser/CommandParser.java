package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.parser;

import java.util.Arrays;
import java.util.List;

public class CommandParser {

    public static ParsedCommand parse(String messageText) {
        String[] parts = messageText.trim().split(" ");
        String commandName = parts[0];
        List<String> args = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
        return new ParsedCommand(commandName, args);
    }
}