package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.parser.user;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegisterCommand implements Command {
    private final AppUserService appUserService;
    private final MessageSender messageSender;

    public RegisterCommand(AppUserService appUserService, MessageSender messageSender) {
        this.appUserService = appUserService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(String chatId, List<String> args) {
        if (args.size() < 2) {
            messageSender.sendMessage(chatId, "Используйте формат: /register [username] [password]");
            return;
        }
        String username = args.get(0);
        String password = args.get(1);

        try {
            AppUser newUser = new AppUser(username, password);
            appUserService.registerUser(newUser, chatId);
            messageSender.sendMessage(chatId, "Регистрация успешна. Теперь используйте /login для входа.");
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка регистрации: " + e.getMessage());
        }
    }
}