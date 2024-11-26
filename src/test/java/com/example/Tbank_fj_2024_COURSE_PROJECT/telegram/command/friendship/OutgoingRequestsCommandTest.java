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

class OutgoingRequestsCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private OutgoingRequestsCommand outgoingRequestsCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тест на успешное получение исходящих запросов
    @Test
    void execute_OutgoingRequests_Success() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        AppUser target1 = new AppUser();
        target1.setUsername("target1");

        AppUser target2 = new AppUser();
        target2.setUsername("target2");

        List<AppUser> outgoingRequests = List.of(target1, target2);

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(friendshipService.getOutgoingRequests(currentUser.getUsername())).thenReturn(outgoingRequests);

        outgoingRequestsCommand.execute(chatId, null);

        verify(friendshipService, times(1)).getOutgoingRequests(currentUser.getUsername());
        verify(messageSender, times(1)).sendFriendRequestsMenu(chatId, outgoingRequests, false);
        verify(messageSender, never()).sendMessage(eq(chatId), anyString());
    }

    // Тест на случай ошибки при получении исходящих запросов
    @Test
    void execute_OutgoingRequests_Error() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(friendshipService.getOutgoingRequests(currentUser.getUsername())).thenThrow(new RuntimeException("Test exception"));

        outgoingRequestsCommand.execute(chatId, null);

        verify(friendshipService, times(1)).getOutgoingRequests(currentUser.getUsername());
        verify(messageSender, times(1)).sendMessage(chatId, "Произошла ошибка при загрузке исходящих запросов в друзья.");
        verify(messageSender, never()).sendFriendRequestsMenu(eq(chatId), any(), anyBoolean());
    }
}
