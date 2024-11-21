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
        if (combinedPlannedMovies.isEmpty()) {
            logger.info("No planned movies found for user: {}, chatId: {}", currentUser.getUsername(), chatId);
            messageSender.sendMessage(chatId, "У вас нет запланированных фильмов.");
            messageSender.sendMainMenu(chatId);
            return;
        }

        Set<String> addedMovieIds = new HashSet<>();
        StringBuilder response = new StringBuilder("Запланированные фильмы (ваши и предложенные друзьями):\n");

        int index = 1;
        for (UserMovie userMovie : combinedPlannedMovies) {
            Movie movie = userMovie.getMovie();
            String suggestedBy = userMovie.getSuggestedBy();

            if (addedMovieIds.add(movie.getImdbId())) {
                response.append(index++).append(". ").append(movie.getTitle())
                        .append(" (").append(movie.getYear()).append(")");

                if (userMovie.getStatus() == MovieStatus.WANT_TO_WATCH) {
                    response.append(" — запланировано вами\n");
                } else if (userMovie.getStatus() == MovieStatus.WANT_TO_WATCH_BY_FRIEND) {
                    response.append(" — предложено другом ").append(suggestedBy != null ? suggestedBy : "неизвестным пользователем").append("\n");
                }

                logger.debug("Added movie to response: {}, suggestedBy: {}, user: {}, chatId: {}",
                        movie.getTitle(), suggestedBy, currentUser.getUsername(), chatId);
            }
        }

        logger.info("Prepared response with planned movies for user: {}, chatId: {}", currentUser.getUsername(), chatId);
        messageSender.sendMessage(chatId, response.toString());
        sessionService.setUserState(chatId, UserStateEnum.WAITING_PLANNED_MOVIE_NUMBER);
        sessionService.setMovieIsPlanned(chatId, true);
    }
}