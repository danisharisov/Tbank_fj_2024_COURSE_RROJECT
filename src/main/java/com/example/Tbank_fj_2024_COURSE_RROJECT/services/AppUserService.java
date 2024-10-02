package com.example.Tbank_fj_2024_COURSE_RROJECT.services;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_RROJECT.repositories.AppUserRepository;
import com.example.Tbank_fj_2024_COURSE_RROJECT.repositories.MovieRepository;
import com.example.Tbank_fj_2024_COURSE_RROJECT.repositories.UserMovieRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
    private OmdbService omdbService;


    public AppUser findByUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user != null) {
            logger.info("User found by username: {}", username);
        } else {
            logger.warn("User not found by username: {}", username);
        }
        return user;
    }

    public AppUser findByTelegramId(String telegramId) {
        AppUser user = appUserRepository.findByTelegramId(telegramId);
        if (user != null) {
            logger.info("User found by telegramId: {}", telegramId);
        } else {
            logger.warn("User not found by telegramId: {}", telegramId);
        }
        return user;
    }

    public boolean checkPassword(AppUser user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public void addWatchedMovie(String username, Movie movie) {
        AppUser user = findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (movie == null) {
            throw new IllegalArgumentException("Movie not found");
        }
        Movie existingMovie = movieRepository.findByImdbId(movie.getImdbId()).orElseGet(() -> {
            movieRepository.save(movie);
            return movie;
        });
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(existingMovie);
        userMovie.setStatus(MovieStatus.WATCHED); // Задаем статус как 'WATCHED'
        userMovieRepository.save(userMovie);

        Set<UserMovie> watchedMovies = user.getWatchedMovies();
        if (watchedMovies == null) {
            watchedMovies = new HashSet<>();
        }
        watchedMovies.add(userMovie);
        user.setWatchedMovies(watchedMovies);
        appUserRepository.save(user);
        logger.info("Movie added to watched list for user: {}", username);
    }


    public List<Movie> getWatchedMoviesByUser(Long userId) {
        return userMovieRepository.findByUserIdAndStatus(userId, MovieStatus.WATCHED)
                .stream()
                .map(UserMovie::getMovie)
                .collect(Collectors.toList());
    }

    @Transactional
    public Set<Movie> getWatchedMovies(String username) {
        AppUser user = findByUsername(username);
        if (user != null) {
            return userMovieRepository.findByUser(user)
                    .stream()
                    .map(userMovie -> userMovie.getMovie())
                    .collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException("Пользователь не найден.");
        }
    }
}