package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageSenderTest {

    @Mock
    private TelegramLongPollingBot bot;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private MessageSender messageSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMainMenu() throws TelegramApiException {
        String chatId = "123456";
        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendMainMenu(chatId);

        verify(sessionService, times(1)).clearUserState(chatId);
        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendMessage() throws TelegramApiException {
        String chatId = "123456";
        String text = "Test message";
        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendMessage(chatId, text);

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendWatchedMovies() throws TelegramApiException {
        String chatId = "123456";
        Movie movie1 = new Movie();
        movie1.setTitle("Movie 1");
        movie1.setYear("2023");

        Movie movie2 = new Movie();
        movie2.setTitle("Movie 2");
        movie2.setYear("2022");

        List<Movie> watchedMovies = List.of(movie1, movie2);

        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendWatchedMovies(chatId, watchedMovies);

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendSimpleMovieList_NoMovies() throws TelegramApiException {
        String chatId = "123456";
        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendSimpleMovieList(chatId, List.of());

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendMovieDetails() throws TelegramApiException {
        String chatId = "123456";
        Movie movie = new Movie();
        movie.setTitle("Movie 1");
        movie.setYear("2023");

        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendMovieDetails(chatId, movie, 8.0, 7.5);

        verify(bot, times(1)).execute(any(SendMessage.class));
        verify(sessionService, times(1)).setSelectedMovie(chatId, movie);
    }

    @Test
    void testSendFriendsMenu() throws TelegramApiException {
        String chatId = "123456";
        AppUser friend1 = new AppUser();
        friend1.setUsername("friend1");

        AppUser friend2 = new AppUser();
        friend2.setUsername("friend2");

        List<AppUser> friends = List.of(friend1, friend2);

        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendFriendsMenu(chatId, friends);

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendFriendRequestsMenu_IncomingRequests() throws TelegramApiException {
        String chatId = "123456";
        AppUser requester1 = new AppUser();
        requester1.setUsername("requester1");

        AppUser requester2 = new AppUser();
        requester2.setUsername("requester2");

        List<AppUser> incomingRequests = List.of(requester1, requester2);

        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendFriendRequestsMenu(chatId, incomingRequests, true);

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendFriendRequestsMenu_OutgoingRequests() throws TelegramApiException {
        String chatId = "123456";
        AppUser friend1 = new AppUser();
        friend1.setUsername("friend1");

        AppUser friend2 = new AppUser();
        friend2.setUsername("friend2");

        List<AppUser> outgoingRequests = List.of(friend1, friend2);

        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendFriendRequestsMenu(chatId, outgoingRequests, false);

        verify(bot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testSendPlannedMovieDetailsWithOptions() throws TelegramApiException {
        String chatId = "123456";
        AppUser user = new AppUser();
        user.setUsername("user1");

        Movie movie = new Movie();
        movie.setTitle("Planned Movie");
        movie.setYear("2023");
        movie.setImdbRating("7.0");

        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendPlannedMovieDetailsWithOptions(chatId, user, movie, 10, 8.5, true);

        verify(bot, times(1)).execute(any(SendMessage.class));
        verify(sessionService, times(1)).setSelectedMovie(chatId, movie);
    }


    @Test
    void testSendSimpleMovieList_WithMovies() throws TelegramApiException {
        String chatId = "123456";
        Movie movie1 = new Movie();
        movie1.setTitle("Movie 1");
        movie1.setYear("2021");
        movie1.setImdbId("tt1234567");

        Movie movie2 = new Movie();
        movie2.setTitle("Movie 2");
        movie2.setYear("2022");
        movie2.setImdbId("tt7654321");

        List<Movie> movies = List.of(movie1, movie2);

        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.sendSimpleMovieList(chatId, movies);

        verify(bot, times(1)).execute(any(SendMessage.class));
    }


    @Test
    void testProcessAddMovieStatusSelection() throws TelegramApiException {
        String chatId = "123456";
        Message mockMessage = new Message();
        when(bot.execute(any(SendMessage.class))).thenReturn(mockMessage);

        messageSender.processAddMovieStatusSelection(chatId);

        verify(bot, times(1)).execute(any(SendMessage.class));
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_FOR_MOVIE_TITLE);
    }


}
