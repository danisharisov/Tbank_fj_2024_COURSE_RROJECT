package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import lombok.Data;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final Map<String, AppUser> sessions = new HashMap<>();
    private final Map<String, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<String, Movie> selectedMovies = new HashMap<>();


    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    public void createSession(String chatId, AppUser user) {
        sessions.put(chatId, user);
    }

    public AppUser getCurrentUser(String chatId) {
        return sessions.get(chatId);
    }

    public void setUserState(String chatId, UserStateEnum newState) {
        UserState userState = getUserState(chatId);
        if (userState != null) {
            logger.info("Изменение состояния пользователя для chatId {}: {} -> {}", chatId, userState.getState(), newState);
            userState.setState(newState);
            saveUserState(chatId, userState);
        } else {
            userState = new UserState(UserStateEnum.DEFAULT, "");
            logger.warn("Не удалось найти состояние пользователя для chatId {} при попытке установить новое состояние: {}", chatId, newState);
        }
    }



    public void clearUserState(String chatId) {
        userStates.remove(chatId);
    }

    public void saveUserState(String chatId, UserState state) {
        userStates.put(chatId, state);
        logger.info("Сохранение состояния пользователя для chatId {}: {}", chatId, state);
    }

    public UserState getUserState(String chatId) {
        UserState state = userStates.getOrDefault(chatId, new UserState(UserStateEnum.DEFAULT, ""));
        logger.info("Получение состояния пользователя для chatId {}: {}", chatId, state);
        return state;
    }
    public void removeSession(String chatId) {
        sessions.remove(chatId);
    }
    public void setSelectedMovie(String chatId, Movie movie) {
        UserState userState = getUserState(chatId);
        if (userState != null) {
            userState.setSelectedMovie(movie); // Сохранение фильма в состояние
            saveUserState(chatId, userState); // Сохранение обновленного состояния
            logger.info("Фильм успешно сохранен для chatId {}: {}", chatId, movie);
        } else {
            logger.error("Ошибка: не удалось найти состояние пользователя для chatId {}", chatId);
        }
    }
    public Movie getSelectedMovie(String chatId) {
        UserState userState = getUserState(chatId);
        logger.info("Получение выбранного фильма для chatId {}: {}", chatId, userState != null ? userState.getSelectedMovie() : "null");
        return userState != null ? userState.getSelectedMovie() : null;
    }



    // Внутренний класс UserState для хранения состояния и контекста
    @Data
    public static class UserState {
        private UserStateEnum state;
        private String context;

        private Movie selectedMovie;

        public Movie getSelectedMovie() {
            return selectedMovie;
        }

        public void setSelectedMovie(Movie selectedMovie) {
            this.selectedMovie = selectedMovie;
        }

        // Переопределите метод toString() для логирования

        public UserState(UserStateEnum state, String context) {
            this.state = state;
            this.context = context;
        }

        public UserState(UserStateEnum state) {
            this.state = state;
            this.context = "";
        }

        @Override
        public String toString() {
            return "UserState{state='" + state + "', context='" + context + "', selectedMovie=" + (selectedMovie != null ? selectedMovie.getTitle() : "null") + '}';
        }


    }
}
