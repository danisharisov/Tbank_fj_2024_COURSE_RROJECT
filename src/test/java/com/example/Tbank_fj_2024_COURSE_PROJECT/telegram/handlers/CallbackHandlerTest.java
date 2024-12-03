package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.movie.*;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class CallbackHandlerTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private MessageSender messageSender;

    @Mock
    private DeleteMovieCommand deleteMovieCommand;

    @Mock
    private DeletePlannedMovieCommand deletePlannedMovieCommand;

    @Mock
    private SelectMovieCommand selectMovieCommand;

    @Mock
    private ViewWatchedMoviesCommand viewWatchedMoviesCommand;

    @Mock
    private ViewPlannedMoviesCommand viewPlannedMoviesCommand;

    @Mock
    private FriendsMenuCommand friendsMenuCommand;

    @Mock
    private IncomingRequestsCommand incomingRequestsCommand;

    @Mock
    private OutgoingRequestsCommand outgoingRequestsCommand;

    @Mock
    private RejectFriendRequestCommand rejectFriendRequestCommand;

    @Mock
    private CancelFriendRequestCommand cancelFriendRequestCommand;

    @Mock
    private AcceptFriendRequestCommand acceptFriendRequestCommand;

    private CallbackHandler callbackHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        callbackHandler = new CallbackHandler(
                sessionService, messageSender,
                deleteMovieCommand, deletePlannedMovieCommand, selectMovieCommand,
                viewWatchedMoviesCommand, viewPlannedMoviesCommand,
                friendsMenuCommand, incomingRequestsCommand, outgoingRequestsCommand,
                rejectFriendRequestCommand, cancelFriendRequestCommand, acceptFriendRequestCommand
        );
        callbackHandler.init();
    }

    // Тест обработки существующего обратного вызова
    @Test
    void handleCallbackQuery_ViewWatchedMovies() {
        String chatId = "12345";
        String callbackData = "view_watched_movies";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(viewWatchedMoviesCommand, times(1)).execute(chatId, null);
        verifyNoMoreInteractions(messageSender);
    }

    // Тест обработки неизвестного обратного вызова
    @Test
    void handleCallbackQuery_UnknownCallback() {
        String chatId = "12345";
        String callbackData = "unknown_command";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(messageSender, times(1)).sendMessage(chatId, "Неизвестная команда.");
        verifyNoInteractions(viewWatchedMoviesCommand);
    }

    // Тест обработки обратного вызова без авторизации
    @Test
    void handleCallbackQuery_NoAuthorization() {
        String chatId = "12345";
        String callbackData = "view_watched_movies";

        when(sessionService.getCurrentUser(chatId)).thenReturn(null);

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(messageSender, times(1)).sendMessage(chatId, "Нажмите /start для начала работы.");
        verifyNoInteractions(viewWatchedMoviesCommand);
    }

    // Тест обработки обратного вызова "delete_movie"
    @Test
    void handleCallbackQuery_DeleteMovie() {
        String chatId = "12345";
        String callbackData = "delete_movie";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(deleteMovieCommand, times(1)).execute(chatId, null);
        verifyNoMoreInteractions(messageSender);
    }

    // Тест обработки обратного вызова с контекстом "select_movie"
    @Test
    void handleCallbackQuery_SelectMovieWithContext() {
        String chatId = "12345";
        String callbackData = "select_movie";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);
        when(sessionService.getContext(chatId)).thenReturn("tt1234567");

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(selectMovieCommand, times(1)).execute(chatId, List.of("tt1234567"));
        verifyNoMoreInteractions(messageSender);
    }

    // Тест обработки обратного вызова "rate_movie"
    @Test
    void handleCallbackQuery_RateMovie() {
        String chatId = "12345";
        String callbackData = "rate_movie";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(messageSender, times(1)).sendMessage(chatId, "Введите оценку от 0 до 10");
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_MOVIE_RATING);
        verifyNoMoreInteractions(messageSender);
    }

    // Тест обработки обратного вызова "friends_menu"
    @Test
    void handleCallbackQuery_FriendsMenu() {
        String chatId = "12345";
        String callbackData = "friends_menu";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(friendsMenuCommand, times(1)).execute(chatId, null);
        verifyNoMoreInteractions(messageSender);
    }

    // Проверяет успешную обработку callback "add_hype"
    @Test
    void handleCallbackQuery_AddHype() {
        String chatId = "12345";
        String callbackData = "add_hype";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(messageSender, times(1)).sendMessage(chatId, "Введите уровень ажиотажа от 0 до 3 для выбранного фильма:");
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_MOVIE_HYPE);
    }

    // Проверяет обработку callback "accept_request"
    @Test
    void handleCallbackQuery_AcceptRequest() {
        String chatId = "12345";
        String callbackData = "accept_request";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        callbackHandler.handleCallbackQuery(chatId, callbackData);

        verify(acceptFriendRequestCommand, times(1)).execute(chatId, null);
        verifyNoMoreInteractions(messageSender);
    }
}
