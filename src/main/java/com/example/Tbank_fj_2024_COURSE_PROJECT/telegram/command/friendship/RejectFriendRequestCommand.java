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
public class RejectFriendRequestCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(RejectFriendRequestCommand.class);

    private final FriendshipService friendshipService;
    private final FriendsMenuCommand friendsMenuCommand;

    @Autowired
    public RejectFriendRequestCommand(SessionService sessionService, FriendshipService friendshipService,
                                      MessageSender messageSender, FriendsMenuCommand friendsMenuCommand) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
        this.friendsMenuCommand = friendsMenuCommand;
    }

    // Отклонить заявку в друзья
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing RejectFriendRequestCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);
        String requesterUsername = sessionService.getContext(chatId);

        try {
            friendshipService.rejectFriendRequest(currentUser.getUsername(), requesterUsername);
            logger.info("Friend request from {} to {} rejected", requesterUsername, currentUser.getUsername());

            messageSender.sendMessage(chatId, "Запрос от " + requesterUsername + " отклонен.");
            sessionService.clearUserState(chatId);
            friendsMenuCommand.execute(chatId, null);
        } catch (Exception e) {
            logger.error("Error while rejecting friend request from {} to {}: {}", requesterUsername, currentUser.getUsername(), e.getMessage());
            messageSender.sendMessage(chatId, "Произошла ошибка при отклонении запроса.");
        }
    }
}