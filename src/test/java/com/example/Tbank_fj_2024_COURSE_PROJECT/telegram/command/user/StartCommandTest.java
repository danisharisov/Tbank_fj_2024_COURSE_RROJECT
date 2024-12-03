package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.command.user;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class StartCommandTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private SessionService sessionService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private StartCommand startCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Проверяет сценарий регистрации нового пользователя
    @Test
    void execute_NewUserRegistration() {
        String chatId = "12345";
        String username = "User12345";

        when(appUserService.findByTelegramId(chatId)).thenReturn(null);

        startCommand.execute(chatId, List.of());

        verify(appUserService, times(1)).saveUser(any(AppUser.class));
        verify(sessionService, times(1)).createSession(eq(chatId), any(AppUser.class));
        verify(messageSender, times(1)).sendMessage(chatId, "Вы зарегистрированы как новый пользователь!");
        verify(messageSender, times(1)).sendMainMenu(chatId);
    }

    // Проверяет сценарий повторного входа существующего пользователя
    @Test
    void execute_ExistingUserLogin() {
        String chatId = "12345";
        AppUser existingUser = new AppUser();
        existingUser.setTelegramId(chatId);
        existingUser.setUsername("ExistingUser");

        when(appUserService.findByTelegramId(chatId)).thenReturn(existingUser);

        startCommand.execute(chatId, List.of());

        verify(appUserService, never()).saveUser(any(AppUser.class));
        verify(sessionService, times(1)).createSession(eq(chatId), eq(existingUser));
        verify(messageSender, times(1)).sendMessage(chatId, "С возвращением, ExistingUser!");
        verify(messageSender, times(1)).sendMainMenu(chatId);
    }

    // Проверяет обработку ошибки
    @Test
    void execute_ExceptionHandling() {
        String chatId = "12345";

        when(appUserService.findByTelegramId(chatId)).thenThrow(new RuntimeException("Test exception"));

        startCommand.execute(chatId, List.of());

        verify(messageSender, times(1)).sendMessage(chatId, "Произошла ошибка при обработке команды /start: Test exception");
        verify(messageSender, never()).sendMainMenu(chatId);
    }
}
