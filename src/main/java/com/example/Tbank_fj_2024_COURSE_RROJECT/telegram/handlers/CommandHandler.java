package com.example.Tbank_fj_2024_COURSE_RROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.OmdbService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.TelegramAuthService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.telegram.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final TelegramAuthService telegramAuthService;
    private final AppUserService appUserService;
    private final SessionService sessionService;
    private final OmdbService omdbService;
    private final MessageSender messageSender;

    @Autowired
    public CommandHandler(
            @Lazy TelegramAuthService telegramAuthService,
            @Lazy AppUserService appUserService,
            @Lazy SessionService sessionService,
            @Lazy OmdbService omdbService,
            @Lazy MessageSender messageSender) {
        this.telegramAuthService = telegramAuthService;
        this.appUserService = appUserService;
        this.sessionService = sessionService;
        this.omdbService = omdbService;
        this.messageSender = messageSender;
    }

    public void handleAuthCommand(String chatId, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length == 2) {
            String username = parts[1];
            logger.info("Processing auth request for username: {}", username);
            telegramAuthService.sendAuthCode(chatId, username);
        } else {
            messageSender.sendMessage(chatId, "Неверный формат команды. Используйте /auth username");
        }
    }

    public void handleRegisterCommand(String chatId, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length == 3) {
            String username = parts[1];
            String password = parts[2];
            logger.info("Processing registration request for username: {}", username);
            try {
                telegramAuthService.registerUser(chatId, username, password);
                messageSender.sendMessage(chatId, "Регистрация выполнена успешно!");
            } catch (IllegalArgumentException e) {
                messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
            }
        } else {
            messageSender.sendMessage(chatId, "Неверный формат команды. Используйте /register username password");
        }
    }

    public void handleLoginCommand(String chatId, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length == 3) {
            String username = parts[1];
            String password = parts[2];
            logger.info("Processing login request for username: {}", username);
            try {
                telegramAuthService.loginUser(chatId, username, password);
                messageSender.sendMessage(chatId, "Вход выполнен успешно!");
            } catch (IllegalArgumentException e) {
                messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
            }
        } else {
            messageSender.sendMessage(chatId, "Неверный формат команды. Используйте /login username password");
        }
    }

    public void handleAddMovieCommand(String chatId, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length == 2) {
            String movieTitle = parts[1];
            System.out.println(movieTitle);
            AppUser currentUser = sessionService.getCurrentUser(chatId);
            if (currentUser != null) {
                logger.info("Processing add movie request for username: {}", currentUser.getUsername());
                List<Movie> movies = omdbService.searchMoviesByTitle(movieTitle);
                if (movies.isEmpty()) {
                    messageSender.sendMessage(chatId, "Фильмы не найдены.");
                } else {
                    messageSender.sendMovieSelectionMessage(chatId, movies);
                }
            } else {
                messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            }
        } else {
            messageSender.sendMessage(chatId, "Неверный формат команды. Используйте /addmovie movieTitle");
        }
    }

    public void handleWatchedMoviesCommand(String chatId, String messageText) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser != null) {
            logger.info("Processing watched movies request for username: {}", currentUser.getUsername());
            try {
                Set<Movie> watchedMovies = appUserService.getWatchedMovies(currentUser.getUsername());
                if (watchedMovies.isEmpty()) {
                    messageSender.sendMessage(chatId, "Список просмотренных фильмов пуст.");
                } else {
                    StringBuilder message = new StringBuilder("Просмотренные фильмы для пользователя " + currentUser.getUsername() + ":\n\n");
                    int index = 1;
                    for (Movie movie : watchedMovies) {
                        message.append(index++)
                                .append(". ").append(movie.getTitle())
                                .append(" (").append(movie.getYear()).append(")\n");
                    }
                    messageSender.sendMessage(chatId, message.toString());
                }
            } catch (IllegalArgumentException e) {
                messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
            }
        } else {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
        }
    }
}