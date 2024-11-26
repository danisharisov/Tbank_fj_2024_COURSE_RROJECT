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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class ViewPlannedMoviesCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private UserMovieService userMovieService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private ViewPlannedMoviesCommand viewPlannedMoviesCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Проверяет отображение списка запланированных фильмов
    @Test
    void execute_ViewPlannedMoviesSuccess() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie1 = new Movie();
        movie1.setTitle("Movie 1");
        Movie movie2 = new Movie();
        movie2.setTitle("Movie 2");

        List<UserMovie> plannedMovies = new ArrayList<>();
        plannedMovies.add(new UserMovie(user, movie1));
        plannedMovies.add(new UserMovie(user, movie2));

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(userMovieService.getCombinedPlannedMovies(user)).thenReturn(plannedMovies);

        viewPlannedMoviesCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendPlannedMovies(chatId, plannedMovies, user);
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_PLANNED_MOVIE_NUMBER);
        verify(sessionService, times(1)).setMovieIsPlanned(chatId, true);
    }

    // Проверяет случай, когда список запланированных фильмов пуст
    @Test
    void execute_ViewPlannedMoviesEmptyList() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(userMovieService.getCombinedPlannedMovies(user)).thenReturn(new ArrayList<>());

        viewPlannedMoviesCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendPlannedMovies(chatId, new ArrayList<>(), user);
        verify(sessionService, never()).setUserState(chatId, UserStateEnum.WAITING_PLANNED_MOVIE_NUMBER);
        verify(sessionService, never()).setMovieIsPlanned(chatId, true);
    }
}
