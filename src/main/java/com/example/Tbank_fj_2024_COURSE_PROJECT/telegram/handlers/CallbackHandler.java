package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    private final SessionService sessionService;
    private final  MessageSender messageSender;
    private final DeleteMovieCommand deleteMovieCommand;
    private final DeletePlannedMovieCommand deletePlannedMovieCommand;
    private final SelectMovieCommand selectMovieCommand;
    private final ViewWatchedMoviesCommand viewWatchedMoviesCommand;
    private final ViewPlannedMoviesCommand viewPlannedMoviesCommand;
    private final FriendsMenuCommand friendsMenuCommand;
    private final IncomingRequestsCommand incomingRequestsCommand;
    private final OutgoingRequestsCommand outgoingRequestsCommand;
    private final RejectFriendRequestCommand rejectFriendRequestCommand;
    private final CancelFriendRequestCommand cancelFriendRequestCommand;
    private final AcceptFriendRequestCommand acceptFriendRequestCommand;
@Autowired
    public CallbackHandler(SessionService sessionService, MessageSender messageSender, DeleteMovieCommand deleteMovieCommand,
                           DeletePlannedMovieCommand deletePlannedMovieCommand, SelectMovieCommand selectMovieCommand,
                           ViewWatchedMoviesCommand viewWatchedMoviesCommand, ViewPlannedMoviesCommand viewPlannedMoviesCommand,
                           FriendsMenuCommand friendsMenuCommand, IncomingRequestsCommand incomingRequestsCommand,
                           OutgoingRequestsCommand outgoingRequestsCommand, RejectFriendRequestCommand rejectFriendRequestCommand,
                           CancelFriendRequestCommand cancelFriendRequestCommand, AcceptFriendRequestCommand acceptFriendRequestCommand) {
        this.sessionService = sessionService;
        this.messageSender = messageSender;
        this.deleteMovieCommand = deleteMovieCommand;
        this.deletePlannedMovieCommand = deletePlannedMovieCommand;
        this.selectMovieCommand = selectMovieCommand;
        this.viewWatchedMoviesCommand = viewWatchedMoviesCommand;
        this.viewPlannedMoviesCommand = viewPlannedMoviesCommand;
        this.friendsMenuCommand = friendsMenuCommand;
        this.incomingRequestsCommand = incomingRequestsCommand;
        this.outgoingRequestsCommand = outgoingRequestsCommand;
        this.rejectFriendRequestCommand = rejectFriendRequestCommand;
        this.cancelFriendRequestCommand = cancelFriendRequestCommand;
        this.acceptFriendRequestCommand = acceptFriendRequestCommand;
    }


    public void handleCallbackQuery(String chatId, String callbackData) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);

        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            return;
        }

        logger.info("Handling callback query for chatId: {}, callbackData: {}", chatId, callbackData);

        switch (callbackData) {
            case "main_menu":
                messageSender.sendMainMenu(chatId);
                break;
            case "add_movie":
                sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_STATUS_SELECTION);
                messageSender.processAddMovieStatusSelection(chatId);
                break;
            case "selected_planned":
                sessionService.setMovieIsPlanned(chatId, true);
                messageSender.sendMessage(chatId, "Введите название фильма для добавления в запланированные:");
                break;
            case "selected_watched":
                sessionService.setMovieIsPlanned(chatId, false);
                messageSender.sendMessage(chatId, "Введите название фильма для добавления в просмотренные:");
                break;
            case "select_movie":
                selectMovieCommand.execute(chatId, List.of(sessionService.getContext(chatId)));
                break;
            case "rate_movie":
                messageSender.sendMessage(chatId, "Введите оценку от 1.0 до 10.0.");
                sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_RATING);
                break;
            case "delete_movie":
                deleteMovieCommand.execute(chatId, null);
                break;
            case "delete_planned":
                deletePlannedMovieCommand.execute(chatId,null);
                break;
            case "view_watched_movies":
                viewWatchedMoviesCommand.execute(chatId,null);
                break;
            case "view_planned_movies":
                viewPlannedMoviesCommand.execute(chatId,null);
                break;
            case "add_hype":
                messageSender.sendMessage(chatId, "Введите уровень ажиотажа от 0 до 100 для выбранного фильма:");
                sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_HYPE);
                break;
            case "friends_menu":
                friendsMenuCommand.execute(chatId, null);
                break;
            case "delete_friend":
                messageSender.sendMessage(chatId, "Введите имя друга для удаления.");
                sessionService.setUserState(chatId, UserStateEnum.WAITING_FRIEND_DELETION);
                break;
            case "send_friend_request":
                messageSender.sendMessage(chatId, "Введите имя пользователя для добавления в друзья.");
                sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
                break;
            case "incoming_requests":
                incomingRequestsCommand.execute(chatId, null);
                break;
            case "outgoing_requests":
                outgoingRequestsCommand.execute(chatId, null);
                break;
            case "accept_request":
                acceptFriendRequestCommand.execute(chatId,null);
                break;
            case "reject_request":
                rejectFriendRequestCommand.execute(chatId,null);
                break;
            case "cancel_request":
                cancelFriendRequestCommand.execute(chatId,null);
                break;
            default:
                messageSender.sendMessage(chatId, "Неизвестная команда.");
                break;
        }
    }
}
