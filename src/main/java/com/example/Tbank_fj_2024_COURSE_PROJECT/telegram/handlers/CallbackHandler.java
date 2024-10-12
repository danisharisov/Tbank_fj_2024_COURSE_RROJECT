package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
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

    private final AppUserService appUserService;
    private final SessionService sessionService;
    private final OmdbService omdbService;
    private final MessageSender messageSender;
    private final FriendshipService friendshipService;
    private final CommandHandler commandHandler;

    @Autowired
    public CallbackHandler(
            @Lazy AppUserService appUserService,
            @Lazy SessionService sessionService,
            @Lazy OmdbService omdbService,
            @Lazy MessageSender messageSender,
            @Lazy FriendshipService friendshipService,
            @Lazy CommandHandler commandHandler) {
        this.appUserService = appUserService;
        this.sessionService = sessionService;
        this.omdbService = omdbService;
        this.messageSender = messageSender;
        this.friendshipService = friendshipService;
        this.commandHandler = commandHandler;
    }

    public void handleCallbackQuery(String chatId, String callbackData) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа или /register для регистрации.");
            return;
        }
        switch (callbackData) {
            case "delete_movie":
                commandHandler.handleDeleteMovie(chatId);  // Удаление фильма
                break;
            case "rate_movie":
                commandHandler.handleRateMovie(chatId);  // Добавление оценки
                break;
            case "add_movie":
                commandHandler.handleAddMovieCommand(chatId, "Введите название фильма для добавления:");
                break;
            case "view_watched_movies":
                commandHandler.handleWatchedMoviesCommand(chatId);
                break;
            case "friends_menu":
                commandHandler.handleFriendsMenu(chatId);
                break;
            case "friend_requests_menu":
                commandHandler.handleIncomingRequestsCommand(chatId);
                break;
            case "main_menu":
                sessionService.clearUserState(chatId);  // Сбрасываем состояние пользователя
                messageSender.sendMainMenu(chatId);  // Переход на главное меню
                break;
            default:
                if (callbackData.startsWith("remove_planned:")) {
                    String imdbId = callbackData.split(":")[1];
                    commandHandler.handleRemovePlannedMovieCommand(chatId, imdbId);
                } else if (callbackData.startsWith("add_movie:")) {
                    String imdbId = callbackData.split(":")[1];
                    commandHandler.processMovieSelection(chatId, imdbId);
                } else if (callbackData.startsWith("select_movie:")) {
                    // Обработка выбора фильма из списка просмотренных
                    int movieIndex = Integer.parseInt(callbackData.split(":")[1]);
                    commandHandler.handleMovieSelection(chatId, movieIndex);
                } else {
                    messageSender.sendMessage(chatId, "Неизвестная команда.");
                }
        }
    }


    /*   private void handleAddMovie(String chatId, String imdbId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            Movie movie = omdbService.getMovieByImdbId(imdbId);
            if (movie != null) {
                appUserService.addWatchedMovie(currentUser.getUsername(), movie);
                messageSender.sendMessage(chatId, "Фильм " + movie.getTitle() + " добавлен в список просмотренных.");
            } else {
                messageSender.sendMessage(chatId, "Фильм не найден.");
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

     */

    private void handleAcceptFriendRequest(String chatId, String friendUsername) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            friendshipService.confirmFriendRequest(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос от " + friendUsername + " принят!");
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    private void handleRejectFriendRequest(String chatId, String friendUsername) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            friendshipService.rejectFriendRequest(currentUser.getUsername(), friendUsername);
            messageSender.sendMessage(chatId, "Запрос от " + friendUsername + " отклонен.");
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }


    private void handleViewFriends(String chatId) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            messageSender.sendFriendsMenu(chatId, friendshipService.getFriends(currentUser.getUsername()));
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }

    public void handleFriendRequestCallback(String chatId, String callbackData) {
        String[] parts = callbackData.split(":");
        if (parts.length == 2) {
            String action = parts[0]; // Пример: "accept_request", "reject_request", "cancel_request"
            String friendUsername = parts[1];

            AppUser currentUser = sessionService.getCurrentUser(chatId);
            if (currentUser != null) {
                switch (action) {
                    case "accept_request":
                        friendshipService.confirmFriendRequest(currentUser.getUsername(), friendUsername);
                        messageSender.sendMessage(chatId, "Вы подтвердили запрос в друзья от " + friendUsername);
                        break;
                    case "reject_request":
                        friendshipService.rejectFriendRequest(currentUser.getUsername(), friendUsername);
                        messageSender.sendMessage(chatId, "Вы отклонили запрос в друзья от " + friendUsername);
                        break;
                    case "cancel_request":
                        friendshipService.rejectFriendRequest(currentUser.getUsername(), friendUsername);
                        messageSender.sendMessage(chatId, "Вы отменили запрос в друзья для " + friendUsername);
                        break;
                    default:
                        messageSender.sendMessage(chatId, "Неизвестное действие: " + action);
                }
            } else {
                messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            }
        } else {
            messageSender.sendMessage(chatId, "Некорректный формат данных для обработки запроса.");
        }
    }

    private void handleRemoveFriend(String chatId, String friendUsername) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            try {
                friendshipService.removeFriend(currentUser.getUsername(), friendUsername);
                messageSender.sendMessage(chatId, "Пользователь " + friendUsername + " был удален из вашего списка друзей.");
            } catch (IllegalArgumentException e) {
                messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }
}
