package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeletePlannedMovieCommand implements Command {

    private final SessionService sessionService;
    private final UserMovieService userMovieService;
    private final MessageSender messageSender;

    @Autowired
    public DeletePlannedMovieCommand(SessionService sessionService, UserMovieService userMovieService,
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
            messageSender.sendMessage(chatId, "Ошибка: выберите фильм для удаления.");
            return;
        }

        userMovieService.removePlannedMovieAndUpdateFriends(currentUser, selectedMovie);
        messageSender.sendMessage(chatId, "Фильм успешно удален из запланированных.");
        sessionService.setSelectedMovie(chatId, null);
        messageSender.sendMainMenu(chatId);
    }
}
