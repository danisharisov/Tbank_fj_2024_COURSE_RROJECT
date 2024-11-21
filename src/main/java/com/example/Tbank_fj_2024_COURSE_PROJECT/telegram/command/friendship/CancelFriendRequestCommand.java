package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CancelFriendRequestCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(CancelFriendRequestCommand.class);

    private final FriendshipService friendshipService;
    private final FriendsMenuCommand friendsMenuCommand;

    @Autowired
    public CancelFriendRequestCommand(SessionService sessionService, FriendshipService friendshipService,
                                      MessageSender messageSender, FriendsMenuCommand friendsMenuCommand) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
        this.friendsMenuCommand = friendsMenuCommand;
    }

    // Отменить заявку в друзья
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing CancelFriendRequestCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);
        String targetUsername = sessionService.getContext(chatId);

        try {
            logger.info("Attempting to cancel friend request from {} to {}", currentUser.getUsername(), targetUsername);
            friendshipService.cancelFriendRequest(currentUser.getUsername(), targetUsername);

            messageSender.sendMessage(chatId, "Запрос в друзья для " + targetUsername + " отменен.");
            logger.info("Friend request to {} successfully canceled.", targetUsername);

            sessionService.clearUserState(chatId);
            friendsMenuCommand.execute(chatId, null);
        } catch (IllegalArgumentException e) {
            logger.error("Error while canceling friend request: {}", e.getMessage());
            messageSender.sendMessage(chatId, "Ошибка при отмене запроса в друзья: " + e.getMessage());
        }
    }
}