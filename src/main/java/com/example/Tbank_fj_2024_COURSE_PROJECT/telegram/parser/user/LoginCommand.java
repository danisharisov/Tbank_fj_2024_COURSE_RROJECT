package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.parser.user;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class LoginCommand implements Command {
    private final AppUserService appUserService;
    private final SessionService sessionService;
    private final MessageSender messageSender;

    public LoginCommand(AppUserService appUserService, SessionService sessionService, MessageSender messageSender) {
        this.appUserService = appUserService;
        this.sessionService = sessionService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(String chatId, List<String> args) {
        if (args.size() < 2) {
            messageSender.sendMessage(chatId, "Используйте формат: /login [username] [password]");
            return;
        }
        String username = args.get(0);
        String password = args.get(1);

        try {
            AppUser user = appUserService.findByUsername(username);
            if (appUserService.checkPassword(user, password)) {
                sessionService.createSession(chatId, user);
                messageSender.sendMessage(chatId, "Вы успешно вошли как " + username);
                messageSender.sendMainMenu(chatId);
            } else {
                messageSender.sendMessage(chatId, "Неверный пароль. Попробуйте снова.");
            }
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка входа: " + e.getMessage());
        }
    }
}