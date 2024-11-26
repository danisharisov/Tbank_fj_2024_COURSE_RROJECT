package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.Command;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeletePlannedMovieCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(DeletePlannedMovieCommand.class);
    private final UserMovieService userMovieService;

    @Autowired
    public DeletePlannedMovieCommand(SessionService sessionService, UserMovieService userMovieService, MessageSender messageSender) {
        super(sessionService, messageSender);
        this.userMovieService = userMovieService;
    }

    //Удалить запланированный фильм
    @Override
    public void execute(String chatId, List<String> args) {
        logger.info("Executing DeletePlannedMovieCommand for chatId: {}", chatId);

        AppUser currentUser = getCurrentUser(chatId);
        Movie selectedMovie = sessionService.getSelectedMovie(chatId);

        if (selectedMovie == null) {
            logger.warn("No selected movie found for chatId: {}", chatId);
            messageSender.sendMessage(chatId, "Ошибка: выберите фильм для удаления.");
            return;
        }

        try {
            logger.info("Removing planned movie '{}' for user '{}'", selectedMovie.getTitle(), currentUser.getUsername());
            userMovieService.removePlannedMovieAndUpdateFriends(currentUser, selectedMovie);
            messageSender.sendMessage(chatId, "Фильм успешно удален из запланированных.");
            sessionService.setSelectedMovie(chatId, null);
            messageSender.sendMainMenu(chatId);
        } catch (Exception e) {
            logger.error("Error while removing planned movie '{}' for chatId: {}: {}", selectedMovie.getTitle(), chatId, e.getMessage(), e);
            messageSender.sendMessage(chatId, "Произошла ошибка при удалении фильма. Попробуйте позже.");
        }
    }
}
