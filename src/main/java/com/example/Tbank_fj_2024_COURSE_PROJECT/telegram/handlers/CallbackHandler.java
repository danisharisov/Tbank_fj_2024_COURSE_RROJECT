package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class CallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    private final SessionService sessionService;
    private final MessageSender messageSender;
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

    private final Map<String, Consumer<String>> callbackActions = new HashMap<>();

    public CallbackHandler(SessionService sessionService, MessageSender messageSender,
                           DeleteMovieCommand deleteMovieCommand, DeletePlannedMovieCommand deletePlannedMovieCommand,
                           SelectMovieCommand selectMovieCommand, ViewWatchedMoviesCommand viewWatchedMoviesCommand,
                           ViewPlannedMoviesCommand viewPlannedMoviesCommand, FriendsMenuCommand friendsMenuCommand,
                           IncomingRequestsCommand incomingRequestsCommand, OutgoingRequestsCommand outgoingRequestsCommand,
                           RejectFriendRequestCommand rejectFriendRequestCommand, CancelFriendRequestCommand cancelFriendRequestCommand,
                           AcceptFriendRequestCommand acceptFriendRequestCommand) {
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

    @PostConstruct
    public void init() {
        callbackActions.put("main_menu", chatId -> messageSender.sendMainMenu(chatId));
        callbackActions.put("add_movie", chatId -> {
            sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_STATUS_SELECTION);
            messageSender.processAddMovieStatusSelection(chatId);
        });
        callbackActions.put("selected_planned", chatId -> {
            sessionService.setMovieIsPlanned(chatId, true);
            messageSender.sendMessage(chatId, "Введите название фильма для добавления в запланированные:");
        });
        callbackActions.put("selected_watched", chatId -> {
            sessionService.setMovieIsPlanned(chatId, false);
            messageSender.sendMessage(chatId, "Введите название фильма для добавления в просмотренные:");
        });
        callbackActions.put("select_movie", chatId -> selectMovieCommand.execute(chatId, List.of(sessionService.getContext(chatId))));
        callbackActions.put("rate_movie", chatId -> {
            messageSender.sendMessage(chatId, "Введите оценку от 0 до 10");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_RATING);
        });
        callbackActions.put("delete_movie", chatId -> deleteMovieCommand.execute(chatId, null));
        callbackActions.put("delete_planned", chatId -> deletePlannedMovieCommand.execute(chatId, null));
        callbackActions.put("view_watched_movies", chatId -> viewWatchedMoviesCommand.execute(chatId, null));
        callbackActions.put("view_planned_movies", chatId -> viewPlannedMoviesCommand.execute(chatId, null));
        callbackActions.put("add_hype", chatId -> {
            messageSender.sendMessage(chatId, "Введите уровень ажиотажа от 0 до 3 для выбранного фильма:");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_HYPE);
        });
        callbackActions.put("friends_menu", chatId -> friendsMenuCommand.execute(chatId, null));
        callbackActions.put("delete_friend", chatId -> {
            messageSender.sendMessage(chatId, "Введите имя друга для удаления.");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_FRIEND_DELETION);
        });
        callbackActions.put("send_friend_request", chatId -> {
            messageSender.sendMessage(chatId, "Введите имя пользователя для добавления в друзья.");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
        });
        callbackActions.put("incoming_requests", chatId -> incomingRequestsCommand.execute(chatId, null));
        callbackActions.put("outgoing_requests", chatId -> outgoingRequestsCommand.execute(chatId, null));
        callbackActions.put("accept_request", chatId -> acceptFriendRequestCommand.execute(chatId, null));
        callbackActions.put("reject_request", chatId -> rejectFriendRequestCommand.execute(chatId, null));
        callbackActions.put("cancel_request", chatId -> cancelFriendRequestCommand.execute(chatId, null));
    }

    public void handleCallbackQuery(String chatId, String callbackData) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Нажмите /start для начала работы.");
            return;
        }

        logger.info("Handling callback query for chatId: {}, callbackData: {}", chatId, callbackData);

        Consumer<String> action = callbackActions.get(callbackData);
        if (action != null) {
            action.accept(chatId);
        } else {
            messageSender.sendMessage(chatId, "Неизвестная команда.");
            logger.warn("Unknown callback data received: {}", callbackData);
        }
    }
}
