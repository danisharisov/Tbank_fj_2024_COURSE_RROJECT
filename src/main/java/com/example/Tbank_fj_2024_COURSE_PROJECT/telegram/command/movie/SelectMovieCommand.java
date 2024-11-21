package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SelectMovieCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(SelectMovieCommand.class);
    private final UserMovieService userMovieService;
    private final MovieService movieService;

    @Autowired
    public SelectMovieCommand(SessionService sessionService, UserMovieService userMovieService,
                              MessageSender messageSender, MovieService movieService) {
        super(sessionService, messageSender);
        this.userMovieService = userMovieService;
        this.movieService = movieService;
    }

    // Выбор фильма из списка по кнопке
    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = getCurrentUser(chatId);
        logger.info("Executing SelectMovieCommand for user: {}, chatId: {}", currentUser.getUsername(), chatId);

        if (args.isEmpty()) {
            logger.warn("No movie IMDB ID provided by user: {}, chatId: {}", currentUser.getUsername(), chatId);
            messageSender.sendMessage(chatId, "Пожалуйста, выберите фильм из списка.");
            return;
        }

        String imdbId = args.get(0);
        boolean isPlanned = sessionService.getUserState(chatId).isMovieIsPlanned();
        logger.info("Received IMDB ID: {}, isPlanned: {}, chatId: {}", imdbId, isPlanned, chatId);

        Optional<Movie> optionalMovie = movieService.findMovieByImdbId(imdbId);
        Movie movie = optionalMovie.orElseGet(() -> {
            logger.info("Fetching and saving movie by IMDB ID: {}, chatId: {}", imdbId, chatId);
            Movie newMovie = movieService.fetchAndSaveMovie(imdbId);
            if (newMovie == null) {
                logger.error("Movie not found for IMDB ID: {}, chatId: {}", imdbId, chatId);
                messageSender.sendMessage(chatId, "Ошибка: фильм не найден.");
            }
            return newMovie;
        });

        if (movie != null) {
            if (isPlanned) {
                userMovieService.addPlannedMovie(currentUser, movie);
                logger.info("Movie added to planned list for user: {}, movie: {}, chatId: {}", currentUser.getUsername(), movie.getTitle(), chatId);
                messageSender.sendMessage(chatId, "Фильм добавлен в запланированные.");
            } else {
                userMovieService.addWatchedMovie(currentUser, movie, chatId);
                logger.info("Movie added to watched list for user: {}, movie: {}, chatId: {}", currentUser.getUsername(), movie.getTitle(), chatId);
            }
            sessionService.clearUserState(chatId);
            messageSender.sendMainMenu(chatId);
        } else {
            logger.warn("Movie is null after fetching or finding, IMDB ID: {}, chatId: {}", imdbId, chatId);
        }
    }
}
