package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
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
public class PickPlannedMovieCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(PickPlannedMovieCommand.class);
    private final UserMovieService userMovieService;

    @Autowired
    public PickPlannedMovieCommand(SessionService sessionService, UserMovieService userMovieService, MessageSender messageSender) {
        super(sessionService, messageSender);
        this.userMovieService = userMovieService;
    }

    // Выбор запланированного фильма по номеру
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing PickPlannedMovieCommand for chatId: {}", chatId);
        AppUser currentUser = sessionService.getCurrentUser(chatId);

        if (args.isEmpty()) {
            logger.warn("No arguments provided for chatId: {}", chatId);
            messageSender.sendMessage(chatId, "Укажите номер фильма, который хотите выбрать.");
            return;
        }

        int movieIndex;
        try {
            movieIndex = Integer.parseInt(args.get(0));
            logger.info("Parsed movie index: {} for chatId: {}", movieIndex, chatId);
        } catch (NumberFormatException e) {
            logger.warn("Invalid movie index format for chatId: {}, args: {}", chatId, args);
            messageSender.sendMessage(chatId, "Некорректный формат номера фильма. Пожалуйста, введите числовое значение.");
            return;
        }

        List<UserMovie> combinedPlannedMovies = userMovieService.getCombinedPlannedMovies(currentUser);

        if (movieIndex < 1 || movieIndex > combinedPlannedMovies.size()) {
            logger.warn("Invalid movie index: {} for chatId: {}. Combined movie size: {}", movieIndex, chatId, combinedPlannedMovies.size());
            messageSender.sendMessage(chatId, "Некорректный номер. Попробуйте снова.");
        } else {
            UserMovie selectedUserMovie = combinedPlannedMovies.get(movieIndex - 1);
            Movie selectedMovie = selectedUserMovie.getMovie();
            AppUser movieOwner = selectedUserMovie.getUser();

            boolean isOwnMovie = userMovieService.isMovieOwner(currentUser, selectedMovie);
            int userHype = (selectedUserMovie.getHype() != null) ? selectedUserMovie.getHype() : 0;
            double averageFriendHype = userMovieService.getAverageFriendHype(currentUser, selectedMovie);

            logger.info("Selected movie: {}, Owner: {}, Current user: {}, User hype: {}, Avg friend hype: {}",
                    selectedMovie.getTitle(), movieOwner.getUsername(), currentUser.getUsername(), userHype, averageFriendHype);

            messageSender.sendPlannedMovieDetailsWithOptions(chatId, currentUser, selectedMovie, userHype, averageFriendHype, isOwnMovie);
            sessionService.setSelectedMovie(chatId, selectedMovie);
            sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
        }
    }
}