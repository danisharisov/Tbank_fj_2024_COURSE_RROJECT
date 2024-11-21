package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
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
public class DeleteFriendCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(DeleteFriendCommand.class);

    private final FriendshipService friendshipService;

    @Autowired
    public DeleteFriendCommand(SessionService sessionService, FriendshipService friendshipService,
                               MessageSender messageSender) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
    }

    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing DeleteFriendCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);

        if (args.isEmpty()) {
            logger.warn("Friend username not provided by chatId: {}", chatId);
            messageSender.sendMessage(chatId, "Введите имя друга для удаления.");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_FRIEND_DELETION);
            return;
        }

        String friendUsername = args.get(0);

        try {
            logger.info("Attempting to remove friend {} for user {}", friendUsername, currentUser.getUsername());
            friendshipService.removeFriendship(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Пользователь " + friendUsername + " был успешно удален из списка друзей.");
            logger.info("Successfully removed friend {} for user {}", friendUsername, currentUser.getUsername());

            sessionService.clearUserState(chatId);
            messageSender.sendFriendsMenu(chatId, null);
        } catch (IllegalArgumentException e) {
            logger.error("Error while removing friend {}: {}", friendUsername, e.getMessage());
            messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }
}
