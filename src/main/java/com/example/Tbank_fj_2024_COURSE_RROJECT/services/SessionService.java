package com.example.Tbank_fj_2024_COURSE_RROJECT.services;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SessionService {

    private final Map<String, AppUser> sessions = new HashMap<>();

    public void createSession(String chatId, AppUser user) {
        sessions.put(chatId, user);
    }

    public AppUser getCurrentUser(String chatId) {
        return sessions.get(chatId);
    }

    public void removeSession(String chatId) {
        sessions.remove(chatId);
    }
}