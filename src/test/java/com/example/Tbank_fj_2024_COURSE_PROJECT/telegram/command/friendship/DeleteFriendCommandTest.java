package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.friendship;

import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.UserStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DeleteFriendCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private DeleteFriendCommand deleteFriendCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_DeleteFriend_Success() {
        String chatId = "12345";
        String friendUsername = "friend";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);

        deleteFriendCommand.execute(chatId, List.of(friendUsername));

        verify(friendshipService, times(1)).removeFriendship(currentUser.getUsername(), friendUsername);
        verify(messageSender, times(1)).sendMessage(chatId, "Пользователь " + friendUsername + " был успешно удален из списка друзей.");
        verify(sessionService, times(1)).clearUserState(chatId);
        verify(messageSender, times(1)).sendFriendsMenu(chatId, null);
    }

    @Test
    void execute_FriendUsernameNotProvided() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);

        deleteFriendCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendMessage(chatId, "Введите имя друга для удаления.");
        verify(sessionService, times(1)).setUserState(chatId, UserStateEnum.WAITING_FRIEND_DELETION);
        verify(friendshipService, never()).removeFriendship(anyString(), anyString());
    }

    @Test
    void execute_DeleteFriend_Error() {
        String chatId = "12345";
        String friendUsername = "friend";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        doThrow(new IllegalArgumentException("Пользователь не найден")).when(friendshipService)
                .removeFriendship(currentUser.getUsername(), friendUsername);

        deleteFriendCommand.execute(chatId, List.of(friendUsername));

        verify(friendshipService, times(1)).removeFriendship(currentUser.getUsername(), friendUsername);
        verify(messageSender, times(1)).sendMessage(chatId, "Ошибка: Пользователь не найден");
        verify(sessionService, never()).clearUserState(chatId);
        verify(messageSender, never()).sendFriendsMenu(chatId, null);
    }
}
