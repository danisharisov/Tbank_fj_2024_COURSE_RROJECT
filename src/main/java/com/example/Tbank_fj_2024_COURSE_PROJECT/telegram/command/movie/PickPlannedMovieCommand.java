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
public class PickPlannedMovieCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(PickPlannedMovieCommand.class);
    private final SessionService sessionService;
    private final UserMovieService userMovieService;
    private final MessageSender messageSender;

    @Autowired
    public PickPlannedMovieCommand(SessionService sessionService, UserMovieService userMovieService,
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
        // Проверяем, что аргумент args не пустой и содержит индекс
        if (args.isEmpty()) {
            messageSender.sendMessage(chatId, "Укажите номер фильма, который хотите выбрать.");
            return;
        }
        // Преобразуем аргумент args[0] в movieIndex
        int movieIndex;
        try {
            movieIndex = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
            messageSender.sendMessage(chatId, "Некорректный формат номера фильма. Пожалуйста, введите числовое значение.");
            return;
        }

            List<UserMovie> combinedPlannedMovies = userMovieService.getCombinedPlannedMovies(currentUser);
            if (movieIndex < 1 || movieIndex > combinedPlannedMovies.size()) {
                messageSender.sendMessage(chatId, "Некорректный номер. Попробуйте снова.");
            } else {
                UserMovie selectedUserMovie = combinedPlannedMovies.get(movieIndex - 1);
                Movie selectedMovie = selectedUserMovie.getMovie();
                AppUser movieOwner = selectedUserMovie.getUser();

                // Проверка на владельца
                boolean isOwnMovie = userMovieService.isMovieOwner(currentUser, selectedMovie);

                logger.info("Selected movie: {}, Owner: {}, Current user: {}", selectedMovie.getTitle(), movieOwner.getUsername(), currentUser.getUsername());

                int userHype = (selectedUserMovie.getHype() != null) ? selectedUserMovie.getHype() : 0;
                double averageFriendHype = userMovieService.getAverageFriendHype(currentUser, selectedMovie);

                // Передаем параметр isOwnMovie, чтобы кнопка удаления отображалась только для собственных фильмов
                messageSender.sendPlannedMovieDetailsWithOptions(chatId, currentUser, selectedMovie, userHype, averageFriendHype, isOwnMovie);
                sessionService.setSelectedMovie(chatId, selectedMovie);
                sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
        }
    }
}
