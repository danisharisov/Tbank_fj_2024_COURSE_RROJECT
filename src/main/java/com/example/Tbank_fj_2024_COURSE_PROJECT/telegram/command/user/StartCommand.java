package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.user;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class StartCommand implements Command {
    private final AppUserService appUserService;
    private final SessionService sessionService;
    private final MessageSender messageSender;

    @Autowired
    public StartCommand(AppUserService appUserService, SessionService sessionService, MessageSender messageSender) {
        this.appUserService = appUserService;
        this.sessionService = sessionService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(String chatId, List<String> args) {
        try {
            // Получаем username из args
            String username = args.isEmpty() ? null : args.get(0);
            if (username == null || username.isEmpty()) {
                username = "User" + chatId; // Дефолтное имя, если username отсутствует
            }

            // Проверяем, зарегистрирован ли пользователь
            AppUser user = appUserService.findByTelegramId(chatId);
            if (user == null) {
                // Если пользователя нет, создаем нового
                user = new AppUser();
                user.setTelegramId(chatId);
                user.setUsername(username);
                appUserService.saveUser(user);
                messageSender.sendMessage(chatId, "Вы зарегистрированы как новый пользователь!");
            } else {
                messageSender.sendMessage(chatId, "С возвращением, " + user.getUsername() + "!");
            }

            // Создаем или обновляем сессию
            sessionService.createSession(chatId, user);

            // Отправляем приветственное сообщение
            String welcomeMessage = "Привет, " + user.getUsername() + "! 👋\n"
                    + "Я помогу вам управлять вашими фильмами и предложениями друзей.\n\n"
                    + "Вот что я умею:\n"
                    + "🎬 Запланировать просмотр фильмов.\n"
                    + "⭐ Оценить просмотренные фильмы.\n"
                    + "👥 Делиться предложениями фильмов с друзьями.\n\n";
            messageSender.sendMessage(chatId, welcomeMessage);

            // Показываем главное меню
            messageSender.sendMainMenu(chatId);
        } catch (Exception e) {
            messageSender.sendMessage(chatId, "Произошла ошибка при обработке команды /start: " + e.getMessage());
        }
    }
}