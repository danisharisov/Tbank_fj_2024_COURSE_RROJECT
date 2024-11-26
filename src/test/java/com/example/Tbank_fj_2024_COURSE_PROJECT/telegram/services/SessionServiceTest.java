package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService();
    }

    // Проверка создания сессии
    @Test
    void createSession_LoggedUser() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        sessionService.createSession(chatId, user);

        AppUser currentUser = sessionService.getCurrentUser(chatId);
        assertNotNull(currentUser);
        assertEquals("testUser", currentUser.getUsername());

        SessionService.UserState state = sessionService.getUserState(chatId);
        assertNotNull(state);
        assertEquals(UserStateEnum.DEFAULT_LOGGED, state.getState());
    }

    // Проверка создания сессии для неавторизованного пользователя
    @Test
    void createSession_UnloggedUser() {
        String chatId = "12345";

        sessionService.createSession(chatId, null);

        AppUser currentUser = sessionService.getCurrentUser(chatId);
        assertNull(currentUser);

        SessionService.UserState state = sessionService.getUserState(chatId);
        assertNotNull(state);
        assertEquals(UserStateEnum.DEFAULT_UNLOGGED, state.getState());
    }

    // Проверка изменения состояния пользователя
    @Test
    void setUserState() {
        String chatId = "12345";
        sessionService.createSession(chatId, null);

        sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_TITLE);

        SessionService.UserState state = sessionService.getUserState(chatId);
        assertEquals(UserStateEnum.WAITING_FOR_MOVIE_TITLE, state.getState());
    }

    // Проверка сброса состояния пользователя
    @Test
    void clearUserState() {
        String chatId = "12345";
        sessionService.createSession(chatId, null);

        sessionService.setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_TITLE);
        sessionService.clearUserState(chatId);

        SessionService.UserState state = sessionService.getUserState(chatId);
        assertEquals(UserStateEnum.DEFAULT_LOGGED, state.getState());
    }

    // Проверка сохранения выбранного фильма
    @Test
    void setSelectedMovie() {
        String chatId = "12345";
        Movie movie = new Movie();
        movie.setTitle("Test Movie");

        sessionService.createSession(chatId, null);
        sessionService.setSelectedMovie(chatId, movie);

        Movie selectedMovie = sessionService.getSelectedMovie(chatId);
        assertNotNull(selectedMovie);
        assertEquals("Test Movie", selectedMovie.getTitle());
    }

    // Проверка установки и получения контекста
    @Test
    void setContextAndGetContext() {
        String chatId = "12345";
        String context = "Test Context";

        sessionService.createSession(chatId, null);
        sessionService.setContext(chatId, context);

        String retrievedContext = sessionService.getContext(chatId);
        assertEquals("Test Context", retrievedContext);
    }

    // Проверка установки состояния "запланированный фильм"
    @Test
    void setMovieIsPlanned() {
        String chatId = "12345";

        sessionService.createSession(chatId, null);
        sessionService.setMovieIsPlanned(chatId, true);

        SessionService.UserState state = sessionService.getUserState(chatId);
        assertTrue(state.isMovieIsPlanned());
    }

}
