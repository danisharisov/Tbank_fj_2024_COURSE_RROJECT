package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.OmdbService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class AddMovieCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private OmdbService omdbService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private AddMovieCommand addMovieCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Проверяет, что команда отправляет сообщение при отсутствии названия фильма
    @Test
    void execute_NoTitleProvided() {
        String chatId = "12345";
        addMovieCommand.execute(chatId, List.of());
        verify(messageSender, times(1)).sendMessage(chatId, "Пожалуйста, введите название фильма для добавления.");
        verify(omdbService, never()).searchMoviesByTitle(anyString());
    }

    // Проверяет успешный поиск фильмов и отправку списка пользователю
    @Test
    void execute_MoviesFound() {
        String chatId = "12345";
        String movieTitle = "Inception";

        Movie movie1 = new Movie();
        movie1.setTitle("Inception");
        Movie movie2 = new Movie();
        movie2.setTitle("Inception 2");
        List<Movie> movies = List.of(movie1, movie2);

        when(omdbService.searchMoviesByTitle(movieTitle)).thenReturn(movies);

        addMovieCommand.execute(chatId, List.of(movieTitle));

        verify(omdbService, times(1)).searchMoviesByTitle(movieTitle);
        verify(messageSender, times(1)).sendSimpleMovieList(chatId, movies);
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
    }

    // Проверяет сценарий, когда фильмы по запросу не найдены
    @Test
    void execute_NoMoviesFound() {
        String chatId = "12345";
        String movieTitle = "NonExistentMovie";

        when(omdbService.searchMoviesByTitle(movieTitle)).thenReturn(Collections.emptyList());

        addMovieCommand.execute(chatId, List.of(movieTitle));

        verify(omdbService, times(1)).searchMoviesByTitle(movieTitle);
        verify(messageSender, times(1)).sendMessage(chatId, "Фильмы по запросу \"NonExistentMovie\" не найдены.");
    }

    // Проверяет обработку ошибки во время выполнения поиска фильмов
    @Test
    void execute_ErrorDuringSearch() {
        String chatId = "12345";
        String movieTitle = "Inception";

        when(omdbService.searchMoviesByTitle(movieTitle)).thenThrow(new RuntimeException("Test exception"));

        addMovieCommand.execute(chatId, List.of(movieTitle));

        verify(omdbService, times(1)).searchMoviesByTitle(movieTitle);
        verify(messageSender, times(1)).sendMessage(chatId, "Произошла ошибка при поиске фильма. Попробуйте позже.");
    }
}
