package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.user.StartCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnloggedStateHandler {
    @Autowired
    protected MessageSender messageSender;
    @Autowired
    protected StartCommand startCommand;

    public void handleUnloggedState(String chatId, String messageText, String username) {
        if ("/start".equals(messageText)) {
            startCommand.execute(chatId, List.of(username));
        } else {
            messageSender.sendMessage(chatId, "Нажмите /start для начала работы.");
        }
    }
}