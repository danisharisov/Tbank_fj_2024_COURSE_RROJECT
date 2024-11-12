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
public class DeleteFriendCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(DeleteFriendCommand.class);

    private final SessionService sessionService;
    private final FriendshipService friendshipService;
    private final MessageSender messageSender;

    @Autowired
    public DeleteFriendCommand(SessionService sessionService, FriendshipService friendshipService,
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
            messageSender.sendMessage(chatId, "Введите имя друга для удаления.");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_FRIEND_DELETION);
            return;
        }

        String friendUsername = args.get(0);
        try {
            friendshipService.removeFriendship(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Пользователь " + friendUsername + " был успешно удален из списка друзей.");
            sessionService.clearUserState(chatId);
            messageSender.sendFriendsMenu(chatId,null);
        } catch (IllegalArgumentException e) {
            messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }
}
