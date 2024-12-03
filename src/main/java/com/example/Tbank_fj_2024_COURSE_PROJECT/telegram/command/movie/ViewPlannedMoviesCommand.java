package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ViewPlannedMoviesCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(ViewPlannedMoviesCommand.class);
    private final UserMovieService userMovieService;

    @Autowired
    public ViewPlannedMoviesCommand(SessionService sessionService, UserMovieService userMovieService, MessageSender messageSender) {
        super(sessionService, messageSender);
        this.userMovieService = userMovieService;
    }

    // Список запланированных фильмов
    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = getCurrentUser(chatId);
        logger.info("Executing ViewPlannedMoviesCommand for user: {}, chatId: {}", currentUser.getUsername(), chatId);

        List<UserMovie> combinedPlannedMovies = userMovieService.getCombinedPlannedMovies(currentUser);

        messageSender.sendPlannedMovies(chatId, combinedPlannedMovies, currentUser);

        if (!combinedPlannedMovies.isEmpty()) {
            sessionService.setUserState(chatId, UserStateEnum.WAITING_PLANNED_MOVIE_NUMBER);
            sessionService.setMovieIsPlanned(chatId, true);
        }
    }
}