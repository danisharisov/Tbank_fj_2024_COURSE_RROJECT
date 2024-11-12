package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutgoingRequestsCommand implements Command {

    private final SessionService sessionService;
    private final FriendshipService friendshipService;
    private final MessageSender messageSender;

    @Autowired
    public OutgoingRequestsCommand(SessionService sessionService, FriendshipService friendshipService,
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

        List<AppUser> outgoingRequests = friendshipService.getOutgoingRequests(currentUser.getUsername());
        messageSender.sendFriendRequestsMenu(chatId, outgoingRequests, false);
    }
}
