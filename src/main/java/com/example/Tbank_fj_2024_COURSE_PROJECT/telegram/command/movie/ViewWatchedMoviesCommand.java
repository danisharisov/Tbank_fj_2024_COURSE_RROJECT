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
public class ViewWatchedMoviesCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(ViewWatchedMoviesCommand.class);
    private final UserMovieService userMovieService;

    @Autowired
    public ViewWatchedMoviesCommand(SessionService sessionService, UserMovieService userMovieService, MessageSender messageSender) {
        super(sessionService, messageSender);
        this.userMovieService = userMovieService;
    }

    //Список просмотренных фильмов
    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = getCurrentUser(chatId);
        logger.info("Executing ViewWatchedMoviesCommand for user: {}, chatId: {}", currentUser.getUsername(), chatId);

        try {
            List<Movie> watchedMovies = userMovieService.getWatchedMoviesByUserId(currentUser.getId());
            if (watchedMovies.isEmpty()) {
                logger.info("No watched movies found for user: {}, chatId: {}", currentUser.getUsername(), chatId);
                messageSender.sendMessage(chatId, "У вас нет просмотренных фильмов.");
                messageSender.sendMainMenu(chatId);
            } else {
                logger.info("Found {} watched movies for user: {}, chatId: {}", watchedMovies.size(), currentUser.getUsername(), chatId);
                messageSender.sendWatchedMovies(chatId, watchedMovies);
                sessionService.setUserState(chatId, UserStateEnum.WAITING_WATCHED_MOVIE_NUMBER);
                sessionService.setMovieIsPlanned(chatId, false);
            }
        } catch (Exception e) {
            logger.error("Error while fetching watched movies for user: {}, chatId: {}, error: {}", currentUser.getUsername(), chatId, e.getMessage());
            messageSender.sendMessage(chatId, "Произошла ошибка при загрузке просмотренных фильмов. Попробуйте позже.");
        }
    }
}