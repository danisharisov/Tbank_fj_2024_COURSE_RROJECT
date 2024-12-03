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

class PickPlannedMovieCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserMovieService userMovieService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private PickPlannedMovieCommand pickPlannedMovieCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Успешный выбор фильма из запланированных
    @Test
    void execute_SelectPlannedMovieSuccess() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        UserMovie userMovie = new UserMovie();
        userMovie.setMovie(movie);
        userMovie.setHype(5);
        userMovie.setUser(user);

        List<UserMovie> plannedMovies = List.of(userMovie);

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(userMovieService.getCombinedPlannedMovies(user)).thenReturn(plannedMovies);

        pickPlannedMovieCommand.execute(chatId, List.of("1"));

        verify(sessionService).setSelectedMovie(chatId, movie);
        verify(sessionService).setUserState(chatId, UserStateEnum.WAITING_MOVIE_SELECTION_USER);
        verify(messageSender).sendPlannedMovieDetailsWithOptions(eq(chatId), eq(user), eq(movie), eq(5), anyDouble(), anyBoolean());
    }


    // Ошибка: список запланированных фильмов пуст
    @Test
    void execute_SelectPlannedMovieInvalidIndex() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(userMovieService.getCombinedPlannedMovies(user)).thenReturn(List.of());

        pickPlannedMovieCommand.execute(chatId, List.of("1"));

        verify(messageSender).sendMessage(chatId, "Некорректный номер. Попробуйте снова.");
        verify(sessionService, never()).setSelectedMovie(anyString(), any());
    }

    // Ошибка: номер фильма не является числом
    @Test
    void execute_SelectPlannedMovieInvalidFormat() {
        String chatId = "12345";

        pickPlannedMovieCommand.execute(chatId, List.of("abc"));

        verify(messageSender).sendMessage(chatId, "Некорректный формат номера фильма. Пожалуйста, введите числовое значение.");
        verify(sessionService, never()).setSelectedMovie(anyString(), any());
    }

    // Ошибка: пользователь не указал номер фильма
    @Test
    void execute_SelectPlannedMovieNoArgs() {
        String chatId = "12345";

        pickPlannedMovieCommand.execute(chatId, List.of());

        verify(messageSender).sendMessage(chatId, "Укажите номер фильма, который хотите выбрать.");
        verify(sessionService, never()).setSelectedMovie(anyString(), any());
    }
}
