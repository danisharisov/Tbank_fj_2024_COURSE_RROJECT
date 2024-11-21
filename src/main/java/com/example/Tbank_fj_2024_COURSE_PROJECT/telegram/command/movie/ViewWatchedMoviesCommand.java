package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewWatchedMoviesCommand implements Command {

    private final SessionService sessionService;
    private final UserMovieService userMovieService;
    private final MessageSender messageSender;

    @Autowired
    public ViewWatchedMoviesCommand(SessionService sessionService, UserMovieService userMovieService,
                                    MessageSender messageSender) {
        this.sessionService = sessionService;
        this.userMovieService = userMovieService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = sessionService.getCurrentUser(chatId);
        if (currentUser == null) {
            messageSender.sendMessage(chatId, "Вы не авторизованы. Используйте /login для входа.");
            return;
        }

        List<Movie> watchedMovies = userMovieService.getWatchedMoviesByUserId(currentUser.getId());
        if (watchedMovies.isEmpty()) {
            messageSender.sendMessage(chatId, "У вас нет просмотренных фильмов.");
            messageSender.sendMainMenu(chatId);
        } else {
            messageSender.sendWatchedMovies(chatId, watchedMovies);
            sessionService.setUserState(chatId, UserStateEnum.WAITING_WATCHED_MOVIE_NUMBER);
            sessionService.setMovieIsPlanned(chatId,false);

        }
    }
}
