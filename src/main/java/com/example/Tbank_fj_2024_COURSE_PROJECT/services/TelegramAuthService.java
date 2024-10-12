package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class TelegramAuthService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramAuthService.class);

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionService sessionService;

    private final Map<String, String> authCodes = new HashMap<>();

    public void sendAuthCode(String chatId, String username) {
        // Использование Optional для поиска пользователя
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found for username: {}", username);
                    return new IllegalArgumentException("User not found");
                });

        String authCode = generateAuthCode();
        authCodes.put(chatId, authCode);

        logger.info("Auth code sent to chatId: {} for username: {}", chatId, username);
    }

    public void registerUser(String chatId, String username, String password) {
        // Проверка наличия пользователя с использованием Optional
        if (appUserRepository.findByUsername(username).isPresent()) {
            logger.warn("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setTelegramId(chatId);
        appUserRepository.save(user);
        logger.info("User registered: {}", username);
    }

    public void loginUser(String chatId, String username, String password) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        // Сохраняем корректный Telegram ID
        user.setTelegramId(chatId);
        appUserRepository.save(user);

        sessionService.createSession(chatId, user);
        logger.info("User logged in and session created for chatId: {}", chatId);
    }

    private String generateAuthCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
