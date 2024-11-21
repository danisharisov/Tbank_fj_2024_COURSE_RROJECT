package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SendFriendRequestCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(SendFriendRequestCommand.class);

    private final FriendshipService friendshipService;

    @Autowired
    public SendFriendRequestCommand(SessionService sessionService, FriendshipService friendshipService,
                                    MessageSender messageSender) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
    }

    // Отправить заявку в друзья
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing SendFriendRequestCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);

        if (args.isEmpty()) {
            logger.warn("Friend username not provided for chatId: {}", chatId);
            messageSender.sendMessage(chatId, "Пожалуйста, укажите имя друга для отправки запроса.");
            return;
        }

        String friendUsername = args.get(0);
        try {
            friendshipService.addFriendRequest(currentUser.getUsername(), friendUsername);
            logger.info("Friend request sent from {} to {}", currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос на добавление в друзья отправлен!");
        } catch (IllegalArgumentException e) {
            logger.error("Error while sending friend request from {} to {}: {}", currentUser.getUsername(), friendUsername, e.getMessage());
            messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }
}