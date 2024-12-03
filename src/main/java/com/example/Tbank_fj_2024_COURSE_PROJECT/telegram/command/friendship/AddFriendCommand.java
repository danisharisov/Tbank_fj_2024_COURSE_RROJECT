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
public class AddFriendCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(AddFriendCommand.class);

    private final FriendshipService friendshipService;

    @Autowired
    public AddFriendCommand(SessionService sessionService, FriendshipService friendshipService,
                            MessageSender messageSender) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
    }

    // Добавить друга
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing AddFriendCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);

        if (args.isEmpty()) {
            logger.warn("No username provided for AddFriendCommand. Waiting for user input.");
            messageSender.sendMessage(chatId, "Введите имя пользователя для добавления в друзья.");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
            return;
        }

        String friendUsername = args.get(0);

        // Удаляем символ '@', если он есть в начале
        if (friendUsername.startsWith("@")) {
            logger.info("Removing '@' from username: {}", friendUsername);
            friendUsername = friendUsername.substring(1);
        }

        try {
            logger.info("Sending friend request from {} to {}", currentUser.getUsername(), friendUsername);
            friendshipService.addFriendRequest(currentUser.getUsername(), friendUsername);

            messageSender.sendMessage(chatId, "Запрос на добавление в друзья отправлен пользователю " + friendUsername + ".");
            sessionService.clearUserState(chatId);
            logger.info("Friend request sent successfully. Redirecting to main menu.");
            messageSender.sendMainMenu(chatId);
        } catch (IllegalArgumentException e) {
            logger.error("Error adding friend: {}", e.getMessage());
            messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }
}