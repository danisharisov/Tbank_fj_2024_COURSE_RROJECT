package com.example.Tbank_fj_2024_COURSE_RROJECT.services;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_RROJECT.repositories.AppUserRepository;
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
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) {
            logger.warn("User not found for username: {}", username);
            throw new IllegalArgumentException("User not found");
        }

        String authCode = generateAuthCode();
        authCodes.put(chatId, authCode);


        logger.info("Auth code sent to chatId: {} for username: {}", chatId, username);
    }

    public void registerUser(String chatId, String username, String password) {
        if (appUserRepository.findByUsername(username) != null) {
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
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) {
            logger.warn("User not found for username: {}", username);
            throw new IllegalArgumentException("User not found");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Incorrect password for username: {}", username);
            throw new IllegalArgumentException("Incorrect password");
        }

        sessionService.createSession(chatId, user);

        logger.info("User logged in: {}", username);
    }

    private String generateAuthCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}