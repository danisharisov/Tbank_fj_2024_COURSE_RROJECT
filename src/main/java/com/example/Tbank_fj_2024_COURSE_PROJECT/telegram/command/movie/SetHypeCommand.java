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
public class SetHypeCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(SetHypeCommand.class);
    private final UserMovieService userMovieService;

    @Autowired
    public SetHypeCommand(SessionService sessionService, UserMovieService userMovieService, MessageSender messageSender) {
        super(sessionService, messageSender);
        this.userMovieService = userMovieService;
    }

    //Установить ажиотаж фильму
    @Override
    public void execute(String chatId, List<String> args) {
        AppUser currentUser = getCurrentUser(chatId);
        logger.info("Executing SetHypeCommand for user: {}, chatId: {}", currentUser.getUsername(), chatId);

        Movie selectedMovie = sessionService.getSelectedMovie(chatId);
        if (selectedMovie == null) {
            logger.warn("No movie selected for setting hype, user: {}, chatId: {}", currentUser.getUsername(), chatId);
            messageSender.sendMessage(chatId, "Ошибка: не выбран фильм для оценки ажиотажа.");
            sessionService.clearUserState(chatId);
            return;
        }

        if (args.isEmpty()) {
            logger.info("Requesting hype input from user: {}, chatId: {}", currentUser.getUsername(), chatId);
            messageSender.sendMessage(chatId, "Введите уровень ажиотажа от 0 до 3:");
            sessionService.setUserState(chatId, UserStateEnum.WAITING_MOVIE_HYPE);
            return;
        }

        try {
            int hype = Integer.parseInt(args.get(0));
            if (hype < 0 || hype > 3) {
                logger.warn("Invalid hype value provided: {}, user: {}, chatId: {}", hype, currentUser.getUsername(), chatId);
                messageSender.sendMessage(chatId, "Некорректное значение. Введите ажиотаж от 0 до 3.");
                return;
            }

            userMovieService.addHype(currentUser, selectedMovie, hype);
            logger.info("Hype set to {} for movie: {}, user: {}, chatId: {}", hype, selectedMovie.getTitle(), currentUser.getUsername(), chatId);
            messageSender.sendMessage(chatId, "Уровень ажиотажа успешно добавлен.");
            messageSender.sendMainMenu(chatId);
        } catch (NumberFormatException e) {
            logger.error("Invalid number format for hype value: {}, user: {}, chatId: {}", args.get(0), currentUser.getUsername(), chatId);
            messageSender.sendMessage(chatId, "Некорректный формат числа. Введите значение от 0 до 3.");
        }
    }
}