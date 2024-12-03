package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.handlers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.user.StartCommand;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class UnloggedStateHandlerTest {

    @Mock
    private MessageSender messageSender;

    @Mock
    private StartCommand startCommand;

    private UnloggedStateHandler unloggedStateHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unloggedStateHandler = new UnloggedStateHandler();
        unloggedStateHandler.messageSender = messageSender;
        unloggedStateHandler.startCommand = startCommand;
    }

    // Проверяет обработку команды /start
    @Test
    void handleUnloggedState_StartCommand() {
        String chatId = "12345";
        String username = "testUser";

        unloggedStateHandler.handleUnloggedState(chatId, "/start", username);

        verify(startCommand, times(1)).execute(chatId, List.of(username));
        verifyNoInteractions(messageSender);
    }

    // Проверяет обработку любого другого текста
    @Test
    void handleUnloggedState_OtherText() {
        String chatId = "12345";
        String messageText = "Hello";
        String username = "testUser";

        unloggedStateHandler.handleUnloggedState(chatId, messageText, username);

        verify(messageSender, times(1)).sendMessage(chatId, "Нажмите /start для начала работы.");
        verifyNoInteractions(startCommand);
    }
}
