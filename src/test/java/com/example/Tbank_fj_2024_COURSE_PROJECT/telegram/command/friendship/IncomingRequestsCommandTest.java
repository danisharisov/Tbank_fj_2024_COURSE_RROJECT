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

class IncomingRequestsCommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private IncomingRequestsCommand incomingRequestsCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тест на успешное получение входящих запросов
    @Test
    void execute_IncomingRequests_Success() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        AppUser requester1 = new AppUser();
        requester1.setUsername("requester1");

        AppUser requester2 = new AppUser();
        requester2.setUsername("requester2");

        List<AppUser> incomingRequests = List.of(requester1, requester2);

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(friendshipService.getIncomingRequests(currentUser.getUsername())).thenReturn(incomingRequests);

        incomingRequestsCommand.execute(chatId, null);

        verify(friendshipService, times(1)).getIncomingRequests(currentUser.getUsername());
        verify(messageSender, times(1)).sendFriendRequestsMenu(chatId, incomingRequests, true);
        verify(messageSender, never()).sendMessage(eq(chatId), anyString());
    }

    // Тест на случай ошибки при получении входящих запросов
    @Test
    void execute_IncomingRequests_Error() {
        String chatId = "12345";
        AppUser currentUser = new AppUser();
        currentUser.setUsername("currentUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(currentUser);
        when(friendshipService.getIncomingRequests(currentUser.getUsername())).thenThrow(new RuntimeException("Test exception"));

        incomingRequestsCommand.execute(chatId, null);

        verify(friendshipService, times(1)).getIncomingRequests(currentUser.getUsername());
        verify(messageSender, times(1)).sendMessage(chatId, "Произошла ошибка при загрузке входящих запросов в друзья.");
        verify(messageSender, never()).sendFriendRequestsMenu(eq(chatId), any(), anyBoolean());
    }
}
