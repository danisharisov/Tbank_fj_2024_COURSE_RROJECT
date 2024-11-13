package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.parser.user.LoginCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.parser.user.RegisterCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.parser.CommandParser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.parser.ParsedCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnloggedStateHandler {

    private final MessageSender messageSender;
    private final LoginCommand loginCommand;
    private final RegisterCommand registerCommand;

    @Autowired
    public UnloggedStateHandler(LoginCommand loginCommand, RegisterCommand registerCommand, MessageSender messageSender) {;
        this.messageSender = messageSender;
        this.loginCommand = loginCommand;
        this.registerCommand = registerCommand;

    }

    public void handleUnloggedState(String chatId, String messageText) {
        ParsedCommand parsedCommand = CommandParser.parse(messageText);
        String command = parsedCommand.getCommandName();
        List<String> args = parsedCommand.getArgs();

        if ("/login".equals(command)) {
            loginCommand.execute(chatId, args);
        } else if ("/register".equals(command)) {
            registerCommand.execute(chatId, args);
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа или /register для регистрации.");
        }
    }

}