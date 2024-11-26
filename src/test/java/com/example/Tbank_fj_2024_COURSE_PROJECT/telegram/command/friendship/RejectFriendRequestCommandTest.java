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

import static org.mockito.Mockito.*;

class RejectFriendRequestCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @Mock
    private FriendsMenuCommand friendsMenuCommand;

    @InjectMocks
    private RejectFriendRequestCommand rejectFriendRequestCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тест на успешное отклонение заявки
    @Test
    void execute_RejectFriendRequest_Success() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");
        String requesterUsername = "requester";

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(sessionService.getContext(chatId)).thenReturn(requesterUsername);

        rejectFriendRequestCommand.execute(chatId, null);

        verify(friendshipService, times(1)).rejectFriendRequest(currentUser.getUsername(), requesterUsername);
        verify(messageSender, times(1)).sendMessage(chatId, "Запрос от " + requesterUsername + " отклонен.");
        verify(sessionService, times(1)).clearUserState(chatId);
        verify(friendsMenuCommand, times(1)).execute(chatId, null);
    }

    // Тест на случай ошибки при отклонении заявки
    @Test
    void execute_RejectFriendRequest_Error() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");
        String requesterUsername = "requester";

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(sessionService.getContext(chatId)).thenReturn(requesterUsername);
        doThrow(new RuntimeException("Test exception")).when(friendshipService).rejectFriendRequest(currentUser.getUsername(), requesterUsername);

        rejectFriendRequestCommand.execute(chatId, null);

        verify(friendshipService, times(1)).rejectFriendRequest(currentUser.getUsername(), requesterUsername);
        verify(messageSender, times(1)).sendMessage(chatId, "Произошла ошибка при отклонении запроса.");
        verify(sessionService, never()).clearUserState(chatId);
        verify(friendsMenuCommand, never()).execute(chatId, null);
    }
}
