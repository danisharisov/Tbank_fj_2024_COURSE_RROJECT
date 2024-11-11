package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ParsedCommand {
    private String commandName;
    private List<String> args;
}