// command/SelectMovieCommand.java
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
public class SelectMovieCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(SelectMovieCommand.class);

    private final SessionService sessionService;
    private final UserMovieService userMovieService;
    private final MovieService movieService;
    private final MessageSender messageSender;

    @Autowired
    public SelectMovieCommand(SessionService sessionService, UserMovieService userMovieService,
                              MovieService movieService, MessageSender messageSender) {
        this.sessionService = sessionService;
        this.userMovieService = userMovieService;
        this.movieService = movieService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            return;
        }

        if (args.isEmpty()) {
            messageSender.sendMessage(chatId, "Пожалуйста, выберите фильм из списка.");
            return;
        }

        String imdbId = args.get(0);
        boolean isPlanned = sessionService.getUserState(chatId).isMovieIsPlanned();

        Optional<Movie> optionalMovie = movieService.findMovieByImdbId(imdbId);
        Movie movie = optionalMovie.orElseGet(() -> {
            Movie newMovie = movieService.fetchAndSaveMovie(imdbId);
            if (newMovie == null) {
                messageSender.sendMessage(chatId, "Ошибка: фильм не найден.");
            }
            return newMovie;
        });

        if (movie != null) {
            if (isPlanned) {
                userMovieService.addPlannedMovie(currentUser, movie);
                messageSender.sendMessage(chatId, "Фильм добавлен в запланированные.");
            } else {
                userMovieService.addWatchedMovie(currentUser, movie, chatId);
                messageSender.sendMessage(chatId, "Фильм добавлен в просмотренные.");
            }
            sessionService.clearUserState(chatId);
            messageSender.sendMainMenu(chatId);
        }
    }
}
