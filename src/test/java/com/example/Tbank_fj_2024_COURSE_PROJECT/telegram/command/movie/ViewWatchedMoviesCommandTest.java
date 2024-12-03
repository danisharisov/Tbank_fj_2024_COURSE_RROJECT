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

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class ViewWatchedMoviesCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserMovieService userMovieService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private ViewWatchedMoviesCommand viewWatchedMoviesCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Проверяет обработку пустого списка просмотренных фильмов
    @Test
    void execute_NoWatchedMovies() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(userMovieService.getWatchedMoviesByUserId(currentUser.getId())).thenReturn(Collections.emptyList());

        viewWatchedMoviesCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendMessage(chatId, "У вас нет просмотренных фильмов.");
        verify(messageSender, times(1)).sendMainMenu(chatId);
        verify(sessionService, never()).setUserState(eq(chatId), any());
        verify(sessionService, never()).setMovieIsPlanned(eq(chatId), anyBoolean());
    }

    // Проверяет корректную обработку списка просмотренных фильмов
    @Test
    void execute_WatchedMoviesExist() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("testUser");

        Movie movie1 = new Movie();
        movie1.setTitle("Test Movie 1");
        movie1.setYear("2023");

        Movie movie2 = new Movie();
        movie2.setTitle("Test Movie 2");
        movie2.setYear("2022");

        List<Movie> watchedMovies = List.of(movie1, movie2);

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(userMovieService.getWatchedMoviesByUserId(currentUser.getId())).thenReturn(watchedMovies);

        viewWatchedMoviesCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendWatchedMovies(chatId, watchedMovies);
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_WATCHED_MOVIE_NUMBER);
        verify(sessionService, times(1)).setMovieIsPlanned(chatId, false);
        verify(messageSender, never()).sendMessage(chatId, "У вас нет просмотренных фильмов.");
        verify(messageSender, never()).sendMainMenu(chatId);
    }

    // Проверяет обработку исключения при получении списка фильмов
    @Test
    void execute_ExceptionWhileFetchingMovies() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(userMovieService.getWatchedMoviesByUserId(currentUser.getId()))
                .thenThrow(new RuntimeException("Test exception"));

        viewWatchedMoviesCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendMessage(chatId, "Произошла ошибка при загрузке просмотренных фильмов. Попробуйте позже.");
        verify(messageSender, never()).sendWatchedMovies(eq(chatId), any());
        verify(sessionService, never()).setUserState(eq(chatId), any());
        verify(sessionService, never()).setMovieIsPlanned(eq(chatId), anyBoolean());
    }
}
