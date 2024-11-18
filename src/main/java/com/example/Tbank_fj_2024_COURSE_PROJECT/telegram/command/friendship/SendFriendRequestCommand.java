package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SendFriendRequestCommand implements Command {

    private final SessionService sessionService;
    private final FriendshipService friendshipService;
    private final MessageSender messageSender;

    @Autowired
    public SendFriendRequestCommand(SessionService sessionService, FriendshipService friendshipService,
                                    MessageSender messageSender) {
        this.sessionService = sessionService;
        this.friendshipService = friendshipService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            return;
        }

        if (args.isEmpty()) {
            messageSender.sendMessage(chatId, "Пожалуйста, укажите имя друга для отправки запроса.");
            return;
        }

        String friendUsername = args.get(0);
        try {
            friendshipService.addFriendRequest(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос на добавление в друзья отправлен!");
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }
}
