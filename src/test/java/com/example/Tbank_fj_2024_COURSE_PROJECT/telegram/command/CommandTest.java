package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private MessageSender messageSender;

    private Command command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new TestCommand(sessionService, messageSender);
    }

    // Проверяет получение текущего пользователя, если он авторизован
    @Test
    void getCurrentUser_UserAuthorized() {
        String chatId = "12345";
        AppUser user = new AppUser();
        user.setUsername("testUser");

        when(sessionService.getCurrentUser(chatId)).thenReturn(user);

        AppUser result = command.getCurrentUser(chatId);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(sessionService, times(1)).getCurrentUser(chatId);
        verifyNoInteractions(messageSender);
    }

    // Проверяет обработку случая, когда пользователь не авторизован
    @Test
    void getCurrentUser_UserNotAuthorized() {
        String chatId = "12345";

        when(sessionService.getCurrentUser(chatId)).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> command.getCurrentUser(chatId));

        assertEquals("Пользователь не авторизован.", exception.getMessage());
        verify(sessionService, times(1)).getCurrentUser(chatId);
        verify(messageSender, times(1)).sendMessage(chatId, "Нажмите /start для начала работы.");
    }

    // Вспомогательный класс для тестирования абстрактного Command
    static class TestCommand extends Command {
        public TestCommand(SessionService sessionService, MessageSender messageSender) {
            super(sessionService, messageSender);
        }

        @Override
        public void execute(String chatId, List<String> args) {
            // Нереализованный метод
        }
    }
}
