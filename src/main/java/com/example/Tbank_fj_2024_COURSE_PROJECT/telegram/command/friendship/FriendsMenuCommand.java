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
public class FriendsMenuCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(FriendsMenuCommand.class);

    private final FriendshipService friendshipService;

    @Autowired
    public FriendsMenuCommand(SessionService sessionService, FriendshipService friendshipService,
                              MessageSender messageSender) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
    }

    // Меню друзья
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing FriendsMenuCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);

        try {
            List<AppUser> friends = friendshipService.getFriends(currentUser.getUsername());
            logger.info("Retrieved {} friends for user {}", friends.size(), currentUser.getUsername());
            messageSender.sendFriendsMenu(chatId, friends);
        } catch (Exception e) {
            logger.error("Error while retrieving friends for user {}: {}", currentUser.getUsername(), e.getMessage());
            messageSender.sendMessage(chatId, "Произошла ошибка при загрузке списка друзей.");
        }
    }
}
