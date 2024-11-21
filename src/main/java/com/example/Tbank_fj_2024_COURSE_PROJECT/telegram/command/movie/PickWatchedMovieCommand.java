package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
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
public class PickWatchedMovieCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(PickWatchedMovieCommand.class);
    private final UserMovieService userMovieService;

    @Autowired
    public PickWatchedMovieCommand(SessionService sessionService, UserMovieService userMovieService, MessageSender messageSender) {
        super(sessionService, messageSender);
        this.userMovieService = userMovieService;
    }
    // Выбор просмотренного фильма по номеру
    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = getCurrentUser(chatId);
        logger.info("Executing PickWatchedMovieCommand for user: {}, chatId: {}", currentUser.getUsername(), chatId);

        if (args.isEmpty()) {
            logger.warn("No movie index provided for chatId: {}", chatId);
            messageSender.sendMessage(chatId, "Укажите номер фильма, который хотите выбрать.");
            return;
        }

        int movieIndex;
        try {
            movieIndex = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
            logger.warn("Invalid movie index format provided: {}, chatId: {}", args.get(0), chatId);
            messageSender.sendMessage(chatId, "Некорректный формат номера фильма. Пожалуйста, введите числовое значение.");
            return;
        }

        List<UserMovie> watchedMovies = userMovieService.getUserMoviesWithDetails(currentUser);
        if (movieIndex < 1 || movieIndex > watchedMovies.size()) {
            logger.warn("Invalid movie index: {}, chatId: {}", movieIndex, chatId);
            messageSender.sendMessage(chatId, "Некорректный номер. Попробуйте снова.");
        } else {
            UserMovie selectedMovie = watchedMovies.get(movieIndex - 1);
            double averageFriendRating = userMovieService.getAverageFriendRating(currentUser, selectedMovie.getMovie());

            logger.info("Selected movie: {}, User rating: {}, Average friend rating: {}, chatId: {}",
                    selectedMovie.getMovie().getTitle(), selectedMovie.getRating(), averageFriendRating, chatId);

            messageSender.sendMovieDetails(chatId, selectedMovie.getMovie(), selectedMovie.getRating(), averageFriendRating);

            sessionService.setSelectedMovie(chatId, selectedMovie.getMovie());
            sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
        }
    }
}