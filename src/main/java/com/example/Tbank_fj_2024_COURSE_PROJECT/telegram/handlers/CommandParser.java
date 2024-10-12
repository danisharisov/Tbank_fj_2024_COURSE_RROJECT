package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import java.util.Arrays;
import java.util.List;

public class CommandParser {

    public static ParsedCommand parse(String messageText) {
        // Разбиваем текст команды на части по пробелам
        String[] parts = messageText.split(" ");
        String commandName = parts[0]; // Первая часть — это имя команды
        List<String> args = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length)); // Остальные — аргументы

        return new ParsedCommand(commandName, args);
    }
}