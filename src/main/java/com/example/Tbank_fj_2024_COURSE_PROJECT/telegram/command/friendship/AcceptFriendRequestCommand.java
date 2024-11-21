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
public class AcceptFriendRequestCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(AcceptFriendRequestCommand.class);

    private final FriendshipService friendshipService;
    private final FriendsMenuCommand friendsMenuCommand;

    @Autowired
    public AcceptFriendRequestCommand(SessionService sessionService, FriendshipService friendshipService,
                                      MessageSender messageSender, FriendsMenuCommand friendsMenuCommand) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
        this.friendsMenuCommand = friendsMenuCommand;
    }

    // Принять заявку в друзья
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing AcceptFriendRequestCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);

        if (currentUser != null) {
            String friendUsername = sessionService.getContext(chatId);
            logger.info("Accepting friend request from user: {} for current user: {}", friendUsername, currentUser.getUsername());

            friendshipService.acceptFriendRequest(currentUser.getUsername(), friendUsername);

            messageSender.sendMessage(chatId, "Запрос от " + friendUsername + " принят.");
            sessionService.clearUserState(chatId);

            logger.info("Friend request accepted. Redirecting to friends menu for chatId: {}", chatId);
            friendsMenuCommand.execute(chatId, null);
        }
    }
}