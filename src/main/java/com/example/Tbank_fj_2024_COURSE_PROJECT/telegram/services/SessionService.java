package com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    public void createSession(String chatId, AppUser user) {
        sessions.put(chatId, user);
        userStates.put(chatId, new UserState(user != null ? UserStateEnum.DEFAULT_LOGGED : UserStateEnum.DEFAULT_UNLOGGED, ""));
    }

    public AppUser getCurrentUser(String chatId) {
        return sessions.get(chatId);
    }

    public void setUserState(String chatId, UserStateEnum newState) {
        UserState userState = userStates.computeIfAbsent(chatId, k -> {
            logger.warn("Инициализация состояния для chatId {} как DEFAULT_UNLOGGED.", chatId);
            return new UserState(UserStateEnum.DEFAULT_UNLOGGED, "");
        });
        logger.info("Изменение состояния для chatId {}: {} -> {}", chatId, userState.getState(), newState);
        userState.setState(newState);
    }

    public void clearUserState(String chatId) {
        userStates.put(chatId, new UserState(UserStateEnum.DEFAULT_LOGGED, ""));
        logger.info("Состояние пользователя для chatId {} сброшено в DEFAULT_LOGGED (без контекста).", chatId);
    }

    public UserState getUserState(String chatId) {
        return userStates.computeIfAbsent(chatId, k -> {
            logger.info("Состояние для chatId {} не найдено. Инициализируем DEFAULT_UNLOGGED состояние.", chatId);
            return new UserState(UserStateEnum.DEFAULT_UNLOGGED, "");
        });
    }

    public void setSelectedMovie(String chatId, Movie movie) {
        UserState userState = getUserState(chatId);
        userState.setSelectedMovie(movie);
        logger.info("Фильм успешно сохранен для chatId {}: {}", chatId, movie);
    }

    public void setContext(String chatId,String context) {
        UserState userState = getUserState(chatId);
        userState.setContext(context);
    }

    public String getContext(String chatId) {
        UserState userState = getUserState(chatId);
        return userState.getContext();
    }


    public void setMovieIsPlanned(String chatId, boolean isPlanned) {
        UserState userState = getUserState(chatId);
        userState.setMovieIsPlanned(isPlanned);
        logger.info("Категория фильма для chatId {}: {}", chatId);
    }

    public Movie getSelectedMovie(String chatId) {
        return getUserState(chatId).getSelectedMovie();
    }

    @Getter
    @Setter
    public static class UserState {
        private UserStateEnum state;
        private String context;
        private Movie selectedMovie;


        public boolean isMovieIsPlanned() {
            return movieIsPlanned;
        }

        private boolean movieIsPlanned;

        public UserState(UserStateEnum state, String context) {
            this.state = state;
            this.context = context != null ? context : "";
        }

        @Override
        public String toString() {
            return "UserState{state='" + state + "', context='" + context + "', selectedMovie=" + (selectedMovie != null ? selectedMovie.getTitle() : "null") + '}';
        }
    }
}
