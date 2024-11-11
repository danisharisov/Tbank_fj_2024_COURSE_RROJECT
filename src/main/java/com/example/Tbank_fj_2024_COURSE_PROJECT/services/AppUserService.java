package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {

    private static final Logger logger = LoggerFactory.getLogger(AppUserService.class);

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional
    public AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким именем не найден: " + username));
    }



    @Transactional
    public AppUser registerUser(AppUser newUser, String chatId) {
        if (appUserRepository.findByUsername(newUser.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует.");
        }
        newUser.setTelegramId(chatId);
        // Хэшируем пароль перед сохранением
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        appUserRepository.save(newUser);
        logger.info("Новый пользователь зарегистрирован: {}", newUser.getUsername());
        return newUser;
    }

    @Transactional
    public boolean checkPassword(AppUser user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}
