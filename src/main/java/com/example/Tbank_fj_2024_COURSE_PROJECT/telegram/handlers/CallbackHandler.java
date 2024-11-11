package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    private final SessionService sessionService;
    private final OmdbService omdbService;
    private final MessageSender messageSender;
    private final CommandHandler commandHandler;

    @Autowired
    public CallbackHandler(
            @Lazy SessionService sessionService,
            @Lazy OmdbService omdbService,
            @Lazy MessageSender messageSender,
            @Lazy CommandHandler commandHandler) {
        this.sessionService = sessionService;
        this.omdbService = omdbService;
        this.messageSender = messageSender;
        this.commandHandler = commandHandler;
    }

    public void handleCallbackQuery(String chatId, String callbackData) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа или /register для регистрации.");
            return;
        }

            switch (callbackData) {
                case "main_menu":
                    messageSender.sendMainMenu(chatId);
                    break;
                case "delete_planned":
                    commandHandler.handleDeletePlannedMovie(chatId);
                    break;
                case "delete_movie":
                    commandHandler.handleDeleteMovie(chatId);
                    break;
                case "rate_movie":
                    commandHandler.handleRateMovie(chatId);
                    break;
                case "add_movie":
                    sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_STATUS_SELECTION);
                    messageSender.processAddMovieStatusSelection(chatId);
                    break;
                case "view_watched_movies":
                    commandHandler.handleWatchedMoviesCommand(chatId);
                    break;
                case  "view_planned_movies":
                    commandHandler.handleShowCombinedPlannedMovies(chatId);
                    break;
                case "friends_menu":
                    commandHandler.handleFriendsMenu(chatId);
                    break;
                case "send_friend_request":
                    sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
                    messageSender.sendMessage(chatId, "Введите имя друга для отправки запроса:");
                    break;
                case "incoming_requests":
                    commandHandler.handleIncomingRequestsCommand(chatId);
                    break;
                case "outgoing_requests":
                    commandHandler.handleOutgoingRequestsCommand(chatId);
                    break;
                case "delete_friend":
                    commandHandler.handleDeleteFriend(chatId);
                    break;
                default:
                    messageSender.sendMessage(chatId, "Неизвестная команда.");
                    break;
            }
        }
    }




