package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class SendFriendRequestCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private SendFriendRequestCommand sendFriendRequestCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тест на успешную отправку запроса в друзья
    @Test
    void execute_SendFriendRequest_Success() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");
        String friendUsername = "friend";

        // Настройка моков
        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);

        // Выполнение команды
        sendFriendRequestCommand.execute(chatId, List.of(friendUsername));

        // Проверка вызовов
        verify(friendshipService, times(1)).addFriendRequest(currentUser.getUsername(), friendUsername);
        verify(messageSender, times(1)).sendMessage(chatId, "Запрос на добавление в друзья отправлен!");
        verify(messageSender, never()).sendMessage(eq(chatId), contains("Ошибка"));
    }

    // Тест на случай, когда имя друга не указано
    @Test
    void execute_FriendUsernameNotProvided() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);

        sendFriendRequestCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendMessage(chatId, "Пожалуйста, укажите имя друга для отправки запроса.");
        verify(friendshipService, never()).addFriendRequest(anyString(), anyString());
    }

    // Тест на случай ошибки при отправке запроса
    @Test
    void execute_SendFriendRequest_Error() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");
        String friendUsername = "friend";

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        doThrow(new IllegalArgumentException("Друг уже в списке друзей")).when(friendshipService).addFriendRequest(currentUser.getUsername(), friendUsername);

        sendFriendRequestCommand.execute(chatId, List.of(friendUsername));

        verify(friendshipService, times(1)).addFriendRequest(currentUser.getUsername(), friendUsername);
        verify(messageSender, times(1)).sendMessage(chatId, "Ошибка: Друг уже в списке друзей");
    }
}
