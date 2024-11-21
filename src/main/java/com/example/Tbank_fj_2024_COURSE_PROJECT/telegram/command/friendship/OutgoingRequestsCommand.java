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
public class OutgoingRequestsCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(OutgoingRequestsCommand.class);

    private final FriendshipService friendshipService;

    @Autowired
    public OutgoingRequestsCommand(SessionService sessionService, FriendshipService friendshipService,
                                   MessageSender messageSender) {
        super(sessionService, messageSender);
        this.friendshipService = friendshipService;
    }

    // Исходящие заявки
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing OutgoingRequestsCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);

        try {
            List<AppUser> outgoingRequests = friendshipService.getOutgoingRequests(currentUser.getUsername());
            logger.info("User {} has {} outgoing friend requests", currentUser.getUsername(), outgoingRequests.size());

            messageSender.sendFriendRequestsMenu(chatId, outgoingRequests, false);
        } catch (Exception e) {
            logger.error("Error while retrieving outgoing friend requests for user {}: {}", currentUser.getUsername(), e.getMessage());
            messageSender.sendMessage(chatId, "Произошла ошибка при загрузке исходящих запросов в друзья.");
        }
    }
}
