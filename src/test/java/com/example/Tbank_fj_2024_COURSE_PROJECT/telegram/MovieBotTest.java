package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CallbackHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.CommandHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers.UnloggedStateHandler;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MovieBotTest {

    @Mock
    private CommandHandler commandHandler;

    @Mock
    private CallbackHandler callbackHandler;

    @Mock
    private SessionService sessionService;

    @Mock
    private MessageSender messageSender;

    @Mock
    private UnloggedStateHandler unloggedStateHandler;

    private MovieBot movieBot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        movieBot = new MovieBot(commandHandler, callbackHandler, sessionService, messageSender, unloggedStateHandler);
    }

    @Test
    void testHandleTextMessage_WhenUserIsNotLoggedIn() {
        String chatId = "12345";
        String messageText = "SomeMessage";
        String username = "TestUser";

        when(sessionService.getCurrentUser(chatId)).thenReturn(null);

        movieBot.handleTextMessage(chatId, messageText, username);

        verify(messageSender).sendMessage(chatId, "Вы еще не авторизованы. Нажмите /start для начала работы.");
        verifyNoInteractions(commandHandler);
    }

    @Test
    void testHandleTextMessage_WhenUserIsLoggedIn() {
        String chatId = "12345";
        String messageText = "SomeCommand";
        String username = "TestUser";
        SessionService.UserState userState = new SessionService.UserState(UserStateEnum.DEFAULT_LOGGED,null);
        AppUser mockUser = mock(AppUser.class);

        when(sessionService.getCurrentUser(chatId)).thenReturn(mockUser);
        when(sessionService.getUserState(chatId)).thenReturn(userState);

        movieBot.handleTextMessage(chatId, messageText, username);

        verify(commandHandler).handleStateBasedCommand(chatId, messageText, userState.getState(), username);
    }

    @Test
    void testOnWebhookUpdateReceived_WithMessage() {
        Update update = new Update();
        Message message = new Message();
        message.setText("/start");
        message.setChat(new Chat(12345L, "private"));
        User user = new User(67890L, "TestUser", false);
        user.setUserName("TestUser");
        message.setFrom(user);
        update.setMessage(message);

        when(sessionService.getCurrentUser("12345")).thenReturn(null);

        movieBot.onWebhookUpdateReceived(update);

        verify(unloggedStateHandler).handleUnloggedState("12345", "/start", "TestUser");
    }
    @Test
    void testOnWebhookUpdateReceived_WithCallbackQuery() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("action:data");
        Message message = new Message();
        message.setChat(new Chat(12345L, "private"));
        callbackQuery.setMessage(message);
        update.setCallbackQuery(callbackQuery);

        movieBot.onWebhookUpdateReceived(update);

        verify(sessionService).setContext("12345", "data");
        verify(callbackHandler).handleCallbackQuery("12345", "action");
    }

}
