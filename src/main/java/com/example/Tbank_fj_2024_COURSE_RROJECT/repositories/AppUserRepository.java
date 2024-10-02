package com.example.Tbank_fj_2024_COURSE_RROJECT.repositories;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByUsername(String username);
    AppUser findByTelegramId(String telegramId);
}