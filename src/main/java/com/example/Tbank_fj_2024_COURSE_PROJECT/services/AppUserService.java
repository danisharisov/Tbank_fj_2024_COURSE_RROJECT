package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {
    private static final Logger logger = LoggerFactory.getLogger(AppUserService.class);

    @Autowired
    private AppUserRepository appUserRepository;

    // Ищет пользователя по имени, выбрасывает исключение, если пользователь не найден
    @Transactional
    public AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким именем не найден: " + username));
    }

    // Ищет пользователя по Telegram ID, возвращает null, если пользователь не найден
    @Transactional
    public AppUser findByTelegramId(String telegramId) {
        return appUserRepository.findByTelegramId(telegramId).orElse(null);
    }

    // Сохраняет пользователя в базу данных
    @Transactional
    public void saveUser(AppUser user) {
        appUserRepository.save(user);
    }
}