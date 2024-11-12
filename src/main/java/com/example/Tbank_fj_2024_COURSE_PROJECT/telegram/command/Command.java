package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command;


import java.util.List;

public interface Command {

    void execute(String chatId, List<String> args);
}