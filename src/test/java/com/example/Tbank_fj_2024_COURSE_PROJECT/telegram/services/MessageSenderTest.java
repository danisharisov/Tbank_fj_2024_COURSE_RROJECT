package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.MovieBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class MessageSenderTest {

    @Mock
    private MovieBot movieBot;

    @Mock
    private SessionService sessionService;

    private MessageSender messageSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageSender = new MessageSender(movieBot, sessionService, null);
    }

    // Тест отправки основного меню
    @Test
    void sendMainMenu_Success() throws TelegramApiException {
        String chatId = "12345";

        messageSender.sendMainMenu(chatId);

        verify(sessionService, times(1)).clearUserState(chatId);
        verify(movieBot, times(1)).execute(any(SendMessage.class));
    }

    // Тест отправки списка просмотренных фильмов
    @Test
    void sendWatchedMovies_Success() throws TelegramApiException {
        String chatId = "12345";
        List<Movie> watchedMovies = List.of(new Movie("tt1234567", "Test Movie", "2024"));

        messageSender.sendWatchedMovies(chatId, watchedMovies);

        verify(movieBot, times(1)).execute(any(SendMessage.class));
    }

    // Тест отправки сообщения с пустым списком запланированных фильмов
    @Test
    void sendPlannedMovies_EmptyList() throws TelegramApiException {
        String chatId = "12345";
        AppUser user = new AppUser();

        messageSender.sendPlannedMovies(chatId, Collections.emptyList(), user);

        verify(movieBot, times(2)).execute(any(SendMessage.class));
    }

    // Тест отправки списка запланированных фильмов
    @Test
    void sendPlannedMovies_WithMovies() throws TelegramApiException {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie("tt1234567", "Planned Movie", "2024");
        UserMovie userMovie = new UserMovie();
        userMovie.setMovie(movie);
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        List<UserMovie> plannedMovies = List.of(userMovie);

        messageSender.sendPlannedMovies(chatId, plannedMovies, user);

        verify(movieBot, times(1)).execute(any(SendMessage.class));
    }

    // Тест отправки сообщения с выбором статуса фильма
    @Test
    void processAddMovieStatusSelection_Success() throws TelegramApiException {
        String chatId = "12345";

        messageSender.processAddMovieStatusSelection(chatId);

        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_TITLE);
        verify(movieBot, times(1)).execute(any(SendMessage.class));
    }

    // Тест отправки деталей фильма
    @Test
    void sendMovieDetails_Success() throws TelegramApiException {
        String chatId = "12345";
        Movie movie = new Movie("tt1234567", "Test Movie", "2024");
        movie.setImdbRating("8.0");

        messageSender.sendMovieDetails(chatId, movie, 8.0, 7.5);

        verify(movieBot, times(1)).execute(any(SendMessage.class));
        verify(sessionService, times(1)).setSelectedMovie(chatId, movie);
    }

    // Тест отправки простого списка фильмов
    @Test
    void sendSimpleMovieList_WithMovies() throws TelegramApiException {
        String chatId = "12345";
        List<Movie> movies = List.of(new Movie("tt1234567", "Movie 1", "2024"));

        messageSender.sendSimpleMovieList(chatId, movies);

        verify(movieBot, times(1)).execute(any(SendMessage.class));
    }

    // Тест отправки пустого списка фильмов
    @Test
    void sendSimpleMovieList_EmptyMovies() throws TelegramApiException {
        String chatId = "12345";

        messageSender.sendSimpleMovieList(chatId, Collections.emptyList());

        verify(movieBot, times(1)).execute(any(SendMessage.class));
    }

    // Тест отправки сообщения
    @Test
    void sendMessage_Success() throws TelegramApiException {
        String chatId = "12345";
        String text = "Test message";

        messageSender.sendMessage(chatId, text);

        verify(movieBot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void sendPlannedMovieDetailsWithOptions_OwnMovie() throws TelegramApiException {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");
        movie.setTitle("Test Movie");
        movie.setYear("2024");
        movie.setImdbRating("8.5");
        movie.setPoster("https://example.com/poster.jpg");

        int userHype = 3;
        double averageFriendHype = 2.5;

        messageSender.sendPlannedMovieDetailsWithOptions(chatId, user, movie, userHype, averageFriendHype, true);

        verify(movieBot, times(1)).handlePhotoMessage(eq(chatId), eq("https://example.com/poster.jpg"), anyString(), anyList());
    }

    @Test
    void sendPlannedMovieDetailsWithOptions_FriendSuggestedMovie() throws TelegramApiException {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");
        movie.setTitle("Test Movie");
        movie.setYear("2024");
        movie.setImdbRating("8.5");
        movie.setPoster(null); // Нет постера

        int userHype = 1;
        double averageFriendHype = 3.0;

        messageSender.sendPlannedMovieDetailsWithOptions(chatId, user, movie, userHype, averageFriendHype, false);

        verify(movieBot, times(1)).execute(any(SendMessage.class));
    }

}
