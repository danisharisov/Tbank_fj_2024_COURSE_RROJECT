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

class AcceptFriendRequestCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @Mock
    private FriendsMenuCommand friendsMenuCommand;

    @InjectMocks
    private AcceptFriendRequestCommand acceptFriendRequestCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тест успешного принятия заявки в друзья
    @Test
    void execute_Success() {
        String chatId = "12345";
        String friendUsername = "friend";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(sessionService.getContext(chatId)).thenReturn(friendUsername);

        acceptFriendRequestCommand.execute(chatId, null);

        verify(friendshipService, times(1)).acceptFriendRequest("currentUser", "friend");
        verify(messageSender, times(1)).sendMessage(chatId, "Запрос от " + friendUsername + " принят.");
        verify(sessionService, times(1)).clearUserState(chatId);
        verify(friendsMenuCommand, times(1)).execute(chatId, null);
    }

    // Тест: имя друга отсутствует в контексте
    @Test
    void execute_FriendUsernameNotFound() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(sessionService.getContext(chatId)).thenReturn(null);

        acceptFriendRequestCommand.execute(chatId, null);

        verify(friendshipService, never()).acceptFriendRequest(anyString(), anyString());
        verify(messageSender, times(1)).sendMessage(chatId, "Ошибка: имя друга не найдено.");
        verify(sessionService, never()).clearUserState(chatId);
        verify(friendsMenuCommand, never()).execute(chatId, null);
    }

}
