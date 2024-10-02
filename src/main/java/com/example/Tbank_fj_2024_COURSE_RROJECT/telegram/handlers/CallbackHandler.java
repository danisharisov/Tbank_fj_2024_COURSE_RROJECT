package com.example.Tbank_fj_2024_COURSE_RROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.OmdbService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_RROJECT.telegram.services.MessageSender;
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

    @Autowired
    public CallbackHandler(
            @Lazy AppUserService appUserService,
            @Lazy SessionService sessionService,
            @Lazy OmdbService omdbService,
            @Lazy MessageSender messageSender) {
        this.appUserService = appUserService;
        this.sessionService = sessionService;
        this.omdbService = omdbService;
        this.messageSender = messageSender;
    }

    public void handleCallbackQuery(String chatId, String callbackData) {
        String[] parts = callbackData.split(":");
        if (parts.length == 2 && "movie".equals(parts[0])) {
            String imdbId = parts[1];
            AppUser currentUser = sessionService.getCurrentUser(chatId);
            if (currentUser != null) {
                try {
                    Movie movie = omdbService.getMovieByImdbId(imdbId);
                    if (movie != null) {
                        appUserService.addWatchedMovie(currentUser.getUsername(), movie);
                        messageSender.sendMessage(chatId, "Фильм " + movie.getTitle() + " добавлен в список просмотренных для пользователя " + currentUser.getUsername());
                    } else {
                        messageSender.sendMessage(chatId, "Фильм не найден.");
                    }
                } catch (IllegalArgumentException e) {
                    messageSender.sendMessage(chatId, "Ошибка: " + e.getMessage());
                }
            } else {
                messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            }
        }
    }
}