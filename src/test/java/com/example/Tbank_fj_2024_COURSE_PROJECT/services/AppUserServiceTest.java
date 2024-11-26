package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тестируем успешный поиск пользователя по имени
    @Test
    void findByUsername_UserExists_ReturnsUser() {
        String username = "testUser";
        AppUser user = new AppUser();
        user.setUsername(username);
        when(appUserRepository.findByUsername(username)).thenReturn(Optional.of(user));

        AppUser result = appUserService.findByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(appUserRepository, times(1)).findByUsername(username);
    }

    // Тестируем случай, когда пользователь не найден
    @Test
    void findByUsername_UserNotFound_ThrowsException() {
        String username = "nonExistentUser";
        when(appUserRepository.findByUsername(username)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> appUserService.findByUsername(username));
        assertEquals("Пользователь с таким именем не найден: " + username, exception.getMessage());
        verify(appUserRepository, times(1)).findByUsername(username);
    }

    // Тестируем успешный поиск пользователя по Telegram ID
    @Test
    void findByTelegramId_UserExists_ReturnsUser() {
        String telegramId = "123456";
        AppUser user = new AppUser();
        user.setTelegramId(telegramId);
        when(appUserRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));

        AppUser result = appUserService.findByTelegramId(telegramId);

        assertNotNull(result);
        assertEquals(telegramId, result.getTelegramId());
        verify(appUserRepository, times(1)).findByTelegramId(telegramId);
    }

    // Тестируем случай, когда пользователь с указанным Telegram ID не найден
    @Test
    void findByTelegramId_UserNotFound_ReturnsNull() {
        String telegramId = "123456";
        when(appUserRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());

        AppUser result = appUserService.findByTelegramId(telegramId);

        assertNull(result);
        verify(appUserRepository, times(1)).findByTelegramId(telegramId);
    }

    // Тестируем успешное сохранение пользователя
    @Test
    void saveUser_SavesUserSuccessfully() {
        AppUser user = new AppUser();
        user.setUsername("newUser");

        appUserService.saveUser(user);

        verify(appUserRepository, times(1)).save(user);
    }
}
