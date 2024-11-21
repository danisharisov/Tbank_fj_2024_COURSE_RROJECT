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
public class IncomingRequestsCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(IncomingRequestsCommand.class);

    private final FriendshipService friendshipService;

    @Autowired
    public IncomingRequestsCommand(SessionService sessionService, FriendshipService friendshipService,
                                   MessageSender messageSender) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
    }

    // Входящие заявки
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing IncomingRequestsCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);

        try {
            List<AppUser> incomingRequests = friendshipService.getIncomingRequests(currentUser.getUsername());
            logger.info("User {} has {} incoming friend requests", currentUser.getUsername(), incomingRequests.size());

            messageSender.sendFriendRequestsMenu(chatId, incomingRequests, true);
        } catch (Exception e) {
            logger.error("Error while retrieving incoming friend requests for user {}: {}", currentUser.getUsername(), e.getMessage());
            messageSender.sendMessage(chatId, "Произошла ошибка при загрузке входящих запросов в друзья.");
        }
    }
}