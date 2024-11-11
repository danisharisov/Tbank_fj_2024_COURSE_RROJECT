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
        if (user != null) {
            userStates.put(chatId, new UserState(UserStateEnum.DEFAULT_LOGGED, "")); // Устанавливаем LOGGED состояние
        } else {
            userStates.put(chatId, new UserState(UserStateEnum.DEFAULT_UNLOGGED, "")); // Если почему-то пользователь не найден, UNLOGGED
        }
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
            logger.info("Состояние успешно сохранено для chatId {}: {}", chatId, userState.getState());
        } else {
            userState = new UserState(UserStateEnum.DEFAULT_UNLOGGED, "");
            logger.warn("Не удалось найти состояние пользователя для chatId {}. Устанавливаем DEFAULT_UNLOGGED состояние.");
            saveUserState(chatId, userState);
        }
    }



    public void clearUserState(String chatId) {
        userStates.put(chatId, new UserState(UserStateEnum.DEFAULT_LOGGED, "")); // Оставляем авторизованное состояние, если он был авторизован
        logger.info("Состояние пользователя для chatId {} сброшено в DEFAULT_LOGGED.", chatId);
    }

    public void saveUserState(String chatId, UserState state) {
        userStates.put(chatId, state);
        logger.info("Сохранение состояния пользователя для chatId {}: {}", chatId, state);
    }

    public UserState getUserState(String chatId) {
        UserState state = userStates.computeIfAbsent(chatId, k -> {
            logger.info("Состояние для chatId {} не найдено. Инициализируем DEFAULT_UNLOGGED состояние.", chatId);
            return new UserState(UserStateEnum.DEFAULT_UNLOGGED, ""); // Инициализация нового пользователя как неавторизованного
        });

        logger.info("Текущее состояние пользователя для chatId {}: {}", chatId, state.getState());
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

    public void updateUserState(String chatId, UserStateEnum newState, String context) {
        UserState state = userStates.computeIfAbsent(chatId, k -> new UserState(UserStateEnum.DEFAULT_LOGGED, "")); // Предполагаем, что он уже логинится
        logger.info("Изменение состояния пользователя chatId {}: {} -> {}", chatId, state.getState(), newState);
        state.setState(newState);
        state.setContext(context);
    }

    public void clearSelectedMovie(String chatId) {
        UserState userState = getUserState(chatId);
        userState.setSelectedMovie(null);
        saveUserState(chatId, userState);
        logger.info("Выбранный фильм для chatId {} был очищен.", chatId);
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
