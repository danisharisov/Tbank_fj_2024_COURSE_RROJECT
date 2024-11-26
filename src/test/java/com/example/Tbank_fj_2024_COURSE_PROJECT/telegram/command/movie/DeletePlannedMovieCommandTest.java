package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class DeletePlannedMovieCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserMovieService userMovieService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private DeletePlannedMovieCommand deletePlannedMovieCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    // Проверяет успешное удаление запланированного фильма
    @Test
    void execute_DeletePlannedMovieSuccess() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);
        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        deletePlannedMovieCommand.execute(chatId, List.of());

        verify(userMovieService, times(1)).removePlannedMovieAndUpdateFriends(user, movie);
        verify(messageSender, times(1)).sendMessage(chatId, "Фильм успешно удален из запланированных.");
        verify(sessionService, times(1)).setSelectedMovie(chatId, null);
        verify(messageSender, times(1)).sendMainMenu(chatId);
    }

    // Проверяет обработку ошибки при удалении запланированного фильма
    @Test
    void execute_DeletePlannedMovieError() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);
        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        doThrow(new RuntimeException("Test exception")).when(userMovieService).removePlannedMovieAndUpdateFriends(user, movie);

        deletePlannedMovieCommand.execute(chatId, List.of());

        verify(userMovieService, times(1)).removePlannedMovieAndUpdateFriends(user, movie);
        verify(messageSender, times(1)).sendMessage(chatId, "Произошла ошибка при удалении фильма. Попробуйте позже.");
        verify(sessionService, never()).setSelectedMovie(chatId, null);
        verify(messageSender, never()).sendMainMenu(chatId);
    }
}
