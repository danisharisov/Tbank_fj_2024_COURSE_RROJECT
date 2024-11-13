package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddMovieCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(AddMovieCommand.class);

    private final SessionService sessionService;
    private final OmdbService omdbService;
    private final MessageSender messageSender;

    @Autowired
    public AddMovieCommand(SessionService sessionService, OmdbService omdbService, MessageSender messageSender) {
        this.sessionService = sessionService;
        this.omdbService = omdbService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);

        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа или /register для регистрации.");
            return;
        }

        if (args.isEmpty()) {
            messageSender.sendMessage(chatId, "Пожалуйста, введите название фильма для добавления.");
            return;
        }

        String movieTitle = String.join(" ", args);
        logger.info("Поиск фильма с названием: {}", movieTitle);

        List<Movie> movies = omdbService.searchMoviesByTitle(movieTitle);
        if (!movies.isEmpty()) {
            // Отправляем пользователю список найденных фильмов для выбора
            messageSender.sendSimpleMovieList(chatId, movies);
            // Устанавливаем статус ожидания выбора фильма
                sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
        } else {
            messageSender.sendMessage(chatId, "Фильмы по запросу \"" + movieTitle + "\" не найдены.");
        }
    }
}
