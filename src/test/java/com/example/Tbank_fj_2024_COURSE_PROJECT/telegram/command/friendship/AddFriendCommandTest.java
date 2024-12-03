package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
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

class AddFriendCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private AddFriendCommand addFriendCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тест успешного добавления друга
    @Test
    void execute_Success() {
        String chatId = "12345";
        String friendUsername = "friend";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        // Настройка моков
        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);

        // Выполнение команды
        addFriendCommand.execute(chatId, List.of(friendUsername));

        // Проверка вызовов
        verify(friendshipService, times(1)).addFriendRequest("currentUser", "friend");
        verify(messageSender, times(1)).sendMessage(chatId, "Запрос на добавление в друзья отправлен пользователю friend.");
        verify(sessionService, times(1)).clearUserState(chatId);
        verify(messageSender, times(1)).sendMainMenu(chatId);
    }

    // Тест: отсутствие имени друга в аргументах
    @Test
    void execute_NoUsernameProvided() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        // Настройка моков
        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);

        // Выполнение команды
        addFriendCommand.execute(chatId, List.of());

        // Проверка
        verify(messageSender, times(1)).sendMessage(chatId, "Введите имя пользователя для добавления в друзья.");
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_FOR_FRIEND_USERNAME);
        verify(friendshipService, never()).addFriendRequest(anyString(), anyString());
    }

    // Тест: обработка исключения при добавлении друга
    @Test
    void execute_FriendshipServiceThrowsException() {
        String chatId = "12345";
        String friendUsername = "friend";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        doThrow(new IllegalArgumentException("Пользователь friend не найден."))
                .when(friendshipService).addFriendRequest("currentUser", "friend");

        addFriendCommand.execute(chatId, List.of(friendUsername));

        verify(friendshipService, times(1)).addFriendRequest("currentUser", "friend");
        verify(messageSender, times(1)).sendMessage(chatId, "Ошибка: Пользователь friend не найден.");
        verify(sessionService, never()).clearUserState(chatId);
        verify(messageSender, never()).sendMainMenu(chatId);
    }

    // Тест: удаление символа '@' из имени друга
    @Test
    void execute_RemovesAtSymbol() {
        String chatId = "12345";
        String friendUsername = "@friend";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);

        addFriendCommand.execute(chatId, List.of(friendUsername));

        verify(friendshipService, times(1)).addFriendRequest("currentUser", "friend");
        verify(messageSender, times(1)).sendMessage(chatId, "Запрос на добавление в друзья отправлен пользователю friend.");
        verify(sessionService, times(1)).clearUserState(chatId);
        verify(messageSender, times(1)).sendMainMenu(chatId);
    }
}
