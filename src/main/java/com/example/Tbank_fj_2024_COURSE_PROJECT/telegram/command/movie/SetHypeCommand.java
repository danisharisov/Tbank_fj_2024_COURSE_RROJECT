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
public class SetHypeCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(SetHypeCommand.class);

    private final SessionService sessionService;
    private final UserMovieService userMovieService;
    private final MessageSender messageSender;

    @Autowired
    public SetHypeCommand(SessionService sessionService, UserMovieService userMovieService,
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

        Movie selectedMovie = sessionService.getSelectedMovie(chatId);
        if (selectedMovie == null) {
            messageSender.sendMessage(chatId, "Ошибка: не выбран фильм для оценки ажиотажа.");
            sessionService.clearUserState(chatId);
            return;
        }

        if (args.isEmpty()) {
            messageSender.sendMessage(chatId, "Введите уровень ажиотажа от 0 до 100:");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_HYPE);
            return;
        }

        try {
            int hype = Integer.parseInt(args.get(0));
            if (hype < 0 || hype > 100) {
                messageSender.sendMessage(chatId, "Некорректное значение. Введите ажиотаж от 0 до 100.");
                return;
            }

            userMovieService.addHype(currentUser, selectedMovie, hype);
            messageSender.sendMessage(chatId, "Уровень ажиотажа успешно добавлен.");
            messageSender.sendMainMenu(chatId);
        } catch (NumberFormatException e) {
            messageSender.sendMessage(chatId, "Некорректный формат числа. Введите значение от 0 до 100.");
        }
    }
}
