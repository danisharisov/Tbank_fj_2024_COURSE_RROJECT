package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class CancelFriendRequestCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @Mock
    private FriendsMenuCommand friendsMenuCommand;

    @InjectMocks
    private CancelFriendRequestCommand cancelFriendRequestCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тест: успешная отмена заявки
    @Test
    void execute_Success() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");
        String targetUsername = "targetUser";

        // Настройка моков
        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(sessionService.getContext(chatId)).thenReturn(targetUsername);

        // Выполнение команды
        cancelFriendRequestCommand.execute(chatId, null);

        // Проверка вызовов
        verify(friendshipService, times(1)).cancelFriendRequest("currentUser", "targetUser");
        verify(messageSender, times(1)).sendMessage(chatId, "Запрос в друзья для targetUser отменен.");
        verify(sessionService, times(1)).clearUserState(chatId);
        verify(friendsMenuCommand, times(1)).execute(chatId, null);
    }

    // Тест: ошибка при отмене заявки (IllegalArgumentException)
    @Test
    void execute_FriendshipServiceThrowsException() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");
        String targetUsername = "targetUser";

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(sessionService.getContext(chatId)).thenReturn(targetUsername);
        doThrow(new IllegalArgumentException("Запрос не найден.")).when(friendshipService)
                .cancelFriendRequest("currentUser", "targetUser");

        cancelFriendRequestCommand.execute(chatId, null);

        verify(friendshipService, times(1)).cancelFriendRequest("currentUser", "targetUser");
        verify(messageSender, times(1)).sendMessage(chatId, "Ошибка при отмене запроса в друзья: Запрос не найден.");
        verify(sessionService, never()).clearUserState(chatId);
        verify(friendsMenuCommand, never()).execute(chatId, null);
    }

    // Тест: null в качестве имени друга
    @Test
    void execute_NullTargetUsername() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(sessionService.getContext(chatId)).thenReturn(null);

        cancelFriendRequestCommand.execute(chatId, null);

        verify(friendshipService, never()).cancelFriendRequest(anyString(), anyString());
        verify(messageSender, times(1)).sendMessage(chatId, "Ошибка при отмене запроса в друзья: не указано имя друга.");
        verify(sessionService, never()).clearUserState(chatId);
        verify(friendsMenuCommand, never()).execute(chatId, null);
    }

}
