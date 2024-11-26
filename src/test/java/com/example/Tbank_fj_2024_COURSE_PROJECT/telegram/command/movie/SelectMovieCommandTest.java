package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.MovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class SelectMovieCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserMovieService userMovieService;

    @Mock
    private MovieService movieService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private SelectMovieCommand selectMovieCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Проверяет добавление фильма в запланированные
    @Test
    void execute_AddPlannedMovieSuccess() {
        String chatId = "12345";
        String imdbId = "tt1234567";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setImdbId(imdbId);
        movie.setTitle("Test Movie");

        SessionService.UserState userState = new SessionService.UserState(UserStateEnum.DEFAULT_LOGGED, "");
        userState.setMovieIsPlanned(true);

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getUserState(chatId)).thenReturn(userState);
        when(movieService.findMovieByImdbId(imdbId)).thenReturn(Optional.of(movie));

        selectMovieCommand.execute(chatId, List.of(imdbId));

        verify(userMovieService, times(1)).addPlannedMovie(user, movie);
        verify(messageSender, times(1)).sendMessage(chatId, "Фильм добавлен в запланированные.");
        verify(sessionService, times(1)).clearUserState(chatId);
        verify(messageSender, times(1)).sendMainMenu(chatId);
    }

    // Проверяет добавление фильма в просмотренные
    @Test
    void execute_AddWatchedMovieSuccess() {
        String chatId = "12345";
        String imdbId = "tt1234567";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setImdbId(imdbId);
        movie.setTitle("Test Movie");

        SessionService.UserState userState = new SessionService.UserState(UserStateEnum.DEFAULT_LOGGED, "");
        userState.setMovieIsPlanned(false);

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getUserState(chatId)).thenReturn(userState);
        when(movieService.findMovieByImdbId(imdbId)).thenReturn(Optional.of(movie));

        selectMovieCommand.execute(chatId, List.of(imdbId));

        verify(userMovieService, times(1)).addWatchedMovie(user, movie, chatId);
        verify(messageSender, times(1)).sendMainMenu(chatId);
        verify(sessionService, times(1)).clearUserState(chatId);
    }

    // Проверяет поведение при отсутствии IMDB ID
    @Test
    void execute_NoImdbIdProvided() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        selectMovieCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendMessage(chatId, "Пожалуйста, выберите фильм из списка.");
        verifyNoInteractions(userMovieService, movieService);
    }

    // Проверяет поведение при ошибке получения фильма из базы
    @Test
    void execute_FetchAndSaveMovieError() {
        String chatId = "12345";
        String imdbId = "tt1234567";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        SessionService.UserState userState = new SessionService.UserState(UserStateEnum.DEFAULT_LOGGED, "");
        userState.setMovieIsPlanned(true);

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getUserState(chatId)).thenReturn(userState);
        when(movieService.findMovieByImdbId(imdbId)).thenReturn(Optional.empty());
        when(movieService.fetchAndSaveMovie(imdbId)).thenReturn(null);

        selectMovieCommand.execute(chatId, List.of(imdbId));

        verify(messageSender, times(1)).sendMessage(chatId, "Ошибка: фильм не найден.");
        verifyNoInteractions(userMovieService);
    }
}
