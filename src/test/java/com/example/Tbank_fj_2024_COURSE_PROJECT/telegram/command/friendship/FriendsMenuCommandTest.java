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

class FriendsMenuCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private FriendsMenuCommand friendsMenuCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тест на успешное получение и отображение списка друзей
    @Test
    void execute_FriendsMenu_Success() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        AppUser friend1 = new AppUser();
        friend1.setUsername("friend1");

        AppUser friend2 = new AppUser();
        friend2.setUsername("friend2");

        List<AppUser> friends = List.of(friend1, friend2);

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(friendshipService.getFriends(currentUser.getUsername())).thenReturn(friends);

        friendsMenuCommand.execute(chatId, null);

        verify(friendshipService, times(1)).getFriends(currentUser.getUsername());
        verify(messageSender, times(1)).sendFriendsMenu(chatId, friends);
        verify(messageSender, never()).sendMessage(eq(chatId), anyString());
    }

    // Тест на случай ошибки при получении списка друзей
    @Test
    void execute_FriendsMenu_Error() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(friendshipService.getFriends(currentUser.getUsername())).thenThrow(new RuntimeException("Test exception"));

        friendsMenuCommand.execute(chatId, null);

        verify(friendshipService, times(1)).getFriends(currentUser.getUsername());
        verify(messageSender, times(1)).sendMessage(chatId, "Произошла ошибка при загрузке списка друзей.");
        verify(messageSender, never()).sendFriendsMenu(eq(chatId), any());
    }
}
