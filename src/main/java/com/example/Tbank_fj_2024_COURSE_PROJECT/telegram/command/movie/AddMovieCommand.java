package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.OmdbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddMovieCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(AddMovieCommand.class);
    private final OmdbService omdbService;

    @Autowired
    public AddMovieCommand(SessionService sessionService, OmdbService omdbService, MessageSender messageSender) {
        super(sessionService, messageSender);
        this.omdbService = omdbService;
    }

    // Добавить фильм
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing AddMovieCommand for chatId: {}", chatId);

        if (args.isEmpty()) {
            logger.warn("No movie title provided for chatId: {}", chatId);
            messageSender.sendMessage(chatId, "Пожалуйста, введите название фильма для добавления.");
            return;
        }

        String movieTitle = String.join(" ", args);
        logger.info("Searching for movies with title: \"{}\" for chatId: {}", movieTitle, chatId);

        try {
            List<Movie> movies = omdbService.searchMoviesByTitle(movieTitle);
            if (!movies.isEmpty()) {
                logger.info("Found {} movies for title: \"{}\" for chatId: {}", movies.size(), movieTitle, chatId);
                messageSender.sendSimpleMovieList(chatId, movies);
                sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
            } else {
                logger.warn("No movies found for title: \"{}\" for chatId: {}", movieTitle, chatId);
                messageSender.sendMessage(chatId, "Фильмы по запросу \"" + movieTitle + "\" не найдены.");
            }
        } catch (Exception e) {
            logger.error("Error occurred while searching for movies with title: \"{}\" for chatId: {}: {}", movieTitle, chatId, e.getMessage(), e);
            messageSender.sendMessage(chatId, "Произошла ошибка при поиске фильма. Попробуйте позже.");
        }
    }
}
