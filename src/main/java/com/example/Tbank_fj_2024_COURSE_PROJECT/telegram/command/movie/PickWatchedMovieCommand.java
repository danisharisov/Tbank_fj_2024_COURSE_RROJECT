package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PickWatchedMovieCommand implements Command {
    @Autowired
    private  SessionService sessionService;
    @Autowired
    private UserMovieService userMovieService;
    @Autowired
    private MessageSender messageSender;
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

            List<UserMovie> watchedMovies = userMovieService.getWatchedMovies(currentUser);
            if (movieIndex < 1 || movieIndex > watchedMovies.size()) {
                messageSender.sendMessage(chatId, "Некорректный номер. Попробуйте снова.");
            } else {
                UserMovie selectedMovie = watchedMovies.get(movieIndex - 1);
                double averageFriendRating = userMovieService.getAverageFriendRating(currentUser, selectedMovie.getMovie());
                messageSender.sendMovieDetails(chatId, selectedMovie.getMovie(), selectedMovie.getRating(), averageFriendRating);

                // Сохраняем выбранный фильм и устанавливаем состояние
                sessionService.setSelectedMovie(chatId, selectedMovie.getMovie());
                sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
            }

    }
}
