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

@Component
public class RateMovieCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(RateMovieCommand.class);
    private final UserMovieService userMovieService;

    @Autowired
    public RateMovieCommand(SessionService sessionService, UserMovieService userMovieService, MessageSender messageSender) {
        super(sessionService,messageSender);
        this.userMovieService = userMovieService;
    }

    // Оценка просмотренного фильма
    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = getCurrentUser(chatId);

        Movie selectedMovie = sessionService.getSelectedMovie(chatId);
        if (selectedMovie == null) {
            messageSender.sendMessage(chatId, "Ошибка: выберите фильм для оценки.");
            return;
        }

        if (args.isEmpty()) {
            messageSender.sendMessage(chatId, "Введите оценку от 0 до 10.0.");
            return;
        }

        try {
            double rating = Double.parseDouble(args.get(0));
            if (rating < 0 || rating > 10.0) {
                messageSender.sendMessage(chatId, "Некорректное значение. Введите оценку от 0 до 10.");
                return;
            }

            userMovieService.addRating(currentUser.getUsername(), selectedMovie.getImdbId(), rating);
            messageSender.sendMessage(chatId, "Оценка успешно добавлена.");
            messageSender.sendMainMenu(chatId);

        } catch (NumberFormatException e) {
            messageSender.sendMessage(chatId, "Некорректный формат числа. Введите оценку от 0 до 10.");
        }
    }
}