package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddFriendCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(AddFriendCommand.class);
    @Autowired
    private SessionService sessionService;
    @Autowired
    private  FriendshipService friendshipService;
    @Autowired
    private MessageSender messageSender;


    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            return;
        }

        if (args.isEmpty()) {
            messageSender.sendMessage(chatId, "Введите имя пользователя для добавления в друзья.");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
            return;
        }

        String friendUsername = args.get(0);
        try {
            friendshipService.addFriendRequest(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос на добавление в друзья отправлен пользователю " + friendUsername + ".");
            sessionService.clearUserState(chatId);
            messageSender.sendMainMenu(chatId);
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }
}
