package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.UserMovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppUserService {

    private static final Logger logger = LoggerFactory.getLogger(AppUserService.class);

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserMovieRepository userMovieRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private MessageSender messageSender;


    /**
     * Поиск пользователя по имени
     */
    @Transactional
    public AppUser findByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким именем не найден: " + username));
    }

    /**
     * Поиск пользователя по Telegram ID
     */
    @Transactional
    public AppUser findByTelegramId(String telegramId) {
        logger.info("Получение пользователя по Telegram ID: {}", telegramId);
        return appUserRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден по Telegram ID: " + telegramId));
    }

    /**
     * Добавление фильма в список просмотренных для пользователя
     */
    @Transactional
    public void addWatchedMovie(AppUser user, Movie movie, String chatId) {
        // Ищем фильм у пользователя по IMDB ID через Optional
        Optional<UserMovie> optionalUserMovie = userMovieRepository.findByUserIdAndMovieImdbId(user.getId(), movie.getImdbId());

        // Извлекаем значение или возвращаем null, если объект отсутствует
        UserMovie existingUserMovie = optionalUserMovie.orElse(null);

        if (existingUserMovie != null) {
            if (existingUserMovie.getStatus() == MovieStatus.UNWATCHED) {
                // Если фильм уже в списке, но статус UNWATCHED, меняем статус на WATCHED
                existingUserMovie.setStatus(MovieStatus.WATCHED);
                userMovieRepository.saveAndFlush(existingUserMovie);  // Сохраняем изменения
                messageSender.sendMessage(chatId, "Фильм \"" + movie.getTitle() + "\" теперь помечен как просмотренный.");
            } else {
                // Если фильм уже помечен как просмотренный, сообщаем об этом пользователю
                messageSender.sendMessage(chatId, "Фильм \"" + movie.getTitle() + "\" уже добавлен в ваш список просмотренных.");
            }

            sessionService.clearUserState(chatId);
            messageSender.sendMainMenu(chatId);
            return;
        }

        // Если фильм отсутствует в списке пользователя, проверяем его наличие в базе данных и сохраняем, если нужно
        Movie existingMovie = movieRepository.findByImdbId(movie.getImdbId())
                .orElseGet(() -> movieRepository.save(movie));

        // Создаем новую запись UserMovie и добавляем её в список просмотренных
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(existingMovie);
        userMovie.setStatus(MovieStatus.WATCHED);

        userMovieRepository.saveAndFlush(userMovie);  // Сразу сохраняем изменения
        user.getWatchedMovies().add(userMovie);  // Обновляем только локально, чтобы Hibernate отслеживал состояние

        messageSender.sendMessage(chatId, "Фильм \"" + movie.getTitle() + "\" успешно добавлен в ваш список просмотренных.");
        sessionService.clearUserState(chatId);
        messageSender.sendMainMenu(chatId);
    }







    @Transactional
    public void removePlannedMovie(String username, String imdbId) {
        AppUser user = findByUsername(username);
        Movie movie = movieRepository.findByImdbId(imdbId)
                .orElseThrow(() -> new IllegalArgumentException("Фильм с таким ID не найден: " + imdbId));

        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WANT_TO_WATCH)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке запланированных."));

        user.getPlannedMovies().remove(userMovie);
        userMovieRepository.delete(userMovie);
        appUserRepository.save(user);

        logger.info("Фильм \"{}\" удален из списка запланированных для пользователя: {}", movie.getTitle(), user.getUsername());
    }

    private UserMovie findOrCreateUserMovie(AppUser user, Movie movie, MovieStatus initialStatus) {
        return userMovieRepository.findByUserAndMovie(user, movie)
                .orElseGet(() -> createUserMovie(user, movie, initialStatus));
    }

    private UserMovie createUserMovie(AppUser user, Movie movie, MovieStatus status) {
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(movie);
        userMovie.setStatus(status);
        return userMovieRepository.saveAndFlush(userMovie);
    }

    @Transactional
    public List<Movie> getWatchedMoviesByUser(Long userId) {
        return userMovieRepository.findByUserIdAndStatus(userId, MovieStatus.WATCHED)
                .stream()
                .map(UserMovie::getMovie)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppUser registerUser(AppUser newUser) {
        if (appUserRepository.findByUsername(newUser.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует.");
        }
        // Хэшируем пароль перед сохранением
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        appUserRepository.save(newUser);
        logger.info("Новый пользователь зарегистрирован: {}", newUser.getUsername());
        return newUser;
    }


    /**
     * Проверка пароля для пользователя
     */
    @Transactional
    public boolean checkPassword(AppUser user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}
