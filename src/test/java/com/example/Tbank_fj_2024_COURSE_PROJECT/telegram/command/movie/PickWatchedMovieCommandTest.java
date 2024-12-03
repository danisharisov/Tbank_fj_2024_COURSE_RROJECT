package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
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

class PickWatchedMovieCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserMovieService userMovieService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private PickWatchedMovieCommand pickWatchedMovieCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Успешный выбор просмотренного фильма
    @Test
    void execute_SelectWatchedMovieSuccess() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        UserMovie userMovie = new UserMovie();
        userMovie.setMovie(movie);
        userMovie.setRating(8.0);

        List<UserMovie> watchedMovies = List.of(userMovie);

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(userMovieService.getUserMoviesWithDetails(user)).thenReturn(watchedMovies);
        when(userMovieService.getAverageFriendRating(user, movie)).thenReturn(7.5);

        pickWatchedMovieCommand.execute(chatId, List.of("1"));

        verify(messageSender).sendMovieDetails(chatId, movie, 8.0, 7.5);
        verify(sessionService).setSelectedMovie(chatId, movie);
        verify(sessionService).setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
    }

    // Ошибка при отсутствии индекса фильма
    @Test
    void execute_NoMovieIndexProvided() {
        String chatId = "12345";

        when(sessionService.getCurrentUser(chatId)).thenReturn(new AppUser());

        pickWatchedMovieCommand.execute(chatId, List.of());

        verify(messageSender).sendMessage(chatId, "Укажите номер фильма, который хотите выбрать.");
        verifyNoInteractions(userMovieService);
    }

    // Ошибка при некорректном формате индекса фильма
    @Test
    void execute_InvalidMovieIndexFormat() {
        String chatId = "12345";

        when(sessionService.getCurrentUser(chatId)).thenReturn(new AppUser());

        pickWatchedMovieCommand.execute(chatId, List.of("abc"));

        verify(messageSender).sendMessage(chatId, "Некорректный формат номера фильма. Пожалуйста, введите числовое значение.");
        verifyNoInteractions(userMovieService);
    }

    // Ошибка при некорректном номере фильма
    @Test
    void execute_InvalidMovieIndex() {
        String chatId = "12345";
        AppUser user = new AppUser();

        List<UserMovie> watchedMovies = List.of(new UserMovie());

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(userMovieService.getUserMoviesWithDetails(user)).thenReturn(watchedMovies);

        pickWatchedMovieCommand.execute(chatId, List.of("5"));

        verify(messageSender).sendMessage(chatId, "Некорректный номер. Попробуйте снова.");
    }
}
