package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class SetHypeCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserMovieService userMovieService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private SetHypeCommand setHypeCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Проверяет успешную установку уровня ажиотажа
    @Test
    void execute_SetHypeSuccess() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);

        setHypeCommand.execute(chatId, List.of("2"));

        verify(userMovieService, times(1)).addHype(user, movie, 2);
        verify(messageSender, times(1)).sendMessage(chatId, "Уровень ажиотажа успешно добавлен.");
        verify(messageSender, times(1)).sendMainMenu(chatId);
    }

    // Проверяет обработку ошибки при отсутствии выбранного фильма
    @Test
    void execute_NoSelectedMovie() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getSelectedMovie(chatId)).thenReturn(null);

        setHypeCommand.execute(chatId, List.of("2"));

        verify(messageSender, times(1)).sendMessage(chatId, "Ошибка: не выбран фильм для оценки ажиотажа.");
        verify(sessionService, times(1)).clearUserState(chatId);
        verifyNoInteractions(userMovieService);
    }

    // Проверяет обработку запроса уровня ажиотажа
    @Test
    void execute_RequestHypeInput() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);

        setHypeCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendMessage(chatId, "Введите уровень ажиотажа от 0 до 3:");
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_MOVIE_HYPE);
        verifyNoInteractions(userMovieService);
    }

    // Проверяет обработку некорректного значения ажиотажа
    @Test
    void execute_InvalidHypeValue() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);

        setHypeCommand.execute(chatId, List.of("5"));

        verify(messageSender, times(1)).sendMessage(chatId, "Некорректное значение. Введите ажиотаж от 0 до 3.");
        verifyNoInteractions(userMovieService);
    }

    // Проверяет обработку некорректного формата значения ажиотажа
    @Test
    void execute_InvalidHypeFormat() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getSelectedMovie(chatId)).thenReturn(movie);

        setHypeCommand.execute(chatId, List.of("invalid"));

        verify(messageSender, times(1)).sendMessage(chatId, "Некорректный формат числа. Введите значение от 0 до 3.");
        verifyNoInteractions(userMovieService);
    }
}
