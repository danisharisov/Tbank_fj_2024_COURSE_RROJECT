package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new AppUser();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
    }

    @Test
    void testFindByUsername_UserExists() {
        when(appUserRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        AppUser foundUser = appUserService.findByUsername("testUser");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
        verify(appUserRepository, times(1)).findByUsername("testUser");
    }

    @Test
    void testFindByUsername_UserDoesNotExist() {
        when(appUserRepository.findByUsername("nonexistentUser")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> appUserService.findByUsername("nonexistentUser"));
        verify(appUserRepository, times(1)).findByUsername("nonexistentUser");
    }

    @Test
    void testRegisterUser_NewUser() {
        when(appUserRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        testUser.setUsername("newUser");
        testUser.setPassword("password");
        AppUser registeredUser = appUserService.registerUser(testUser, "123456");

        assertNotNull(registeredUser);
        assertEquals("newUser", registeredUser.getUsername());
        assertEquals("encodedPassword", registeredUser.getPassword());
        assertEquals("123456", registeredUser.getTelegramId());
        verify(appUserRepository, times(1)).save(testUser);
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        when(appUserRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> appUserService.registerUser(testUser, "123456"));
        verify(appUserRepository, never()).save(testUser);
    }

    @Test
    void testCheckPassword_CorrectPassword() {
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        testUser.setPassword("encodedPassword");

        assertTrue(appUserService.checkPassword(testUser, "password"));
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
    }

    @Test
    void testCheckPassword_IncorrectPassword() {
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
        testUser.setPassword("encodedPassword");

        assertFalse(appUserService.checkPassword(testUser, "wrongPassword"));
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
    }
}