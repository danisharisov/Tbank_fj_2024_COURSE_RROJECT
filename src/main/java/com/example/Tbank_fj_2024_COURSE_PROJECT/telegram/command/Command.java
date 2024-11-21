package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command;


import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;

import java.util.List;

public abstract class Command {
    protected final SessionService sessionService;
    protected final MessageSender messageSender;

    public Command(SessionService sessionService, MessageSender messageSender) {
        this.sessionService = sessionService;
        this.messageSender = messageSender;
    }

    public abstract void execute(String chatId, List<String> args);

    protected AppUser getCurrentUser(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Нажмите /start для начала работы.");
            throw new IllegalStateException("Пользователь не авторизован.");
        }
        return currentUser;
    }
}