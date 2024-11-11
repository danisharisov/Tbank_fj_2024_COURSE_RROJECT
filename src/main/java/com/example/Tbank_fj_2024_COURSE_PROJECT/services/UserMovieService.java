package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.UserMovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.RowSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserMovieService {

    private static final Logger logger = LoggerFactory.getLogger(UserMovieService.class);

    @Autowired
    private AppUserService appUserService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserMovieRepository userMovieRepository;
    @Autowired
    @Lazy
    private FriendshipService friendshipService;
    @Autowired
    private MessageSender messageSender;


    @Transactional
    public void addWatchedMovie(AppUser user, Movie movie, String chatId) {
        // Находим существующую запись или создаем новую, не устанавливая сразу статус
        UserMovie userMovie = findOrCreateUserMovie(user, movie);

        if (userMovie.getStatus() == MovieStatus.WATCHED) {
            // Если фильм уже отмечен как просмотренный, уведомляем пользователя
            notifyMovieAlreadyWatched(chatId, movie);
        } else {
            // Если статус отличается, обновляем на "WATCHED"
            updateMovieStatus(userMovie, MovieStatus.WATCHED);
        }
    }

    private void notifyMovieAlreadyWatched(String chatId, Movie movie) {
        messageSender.sendMessage(chatId, "Фильм \"" + movie.getTitle() + "\" уже добавлен в ваш список просмотренных.");
    }

    private void updateMovieStatus(UserMovie userMovie, MovieStatus newStatus) {
        userMovie.setStatus(newStatus);
        userMovieRepository.saveAndFlush(userMovie);
    }

    private UserMovie findOrCreateUserMovie(AppUser user, Movie movie, MovieStatus initialStatus) {
        return userMovieRepository.findByUserAndMovie(user, movie)
                .orElseGet(() -> createUserMovie(user, movie, initialStatus));
    }
    private UserMovie findOrCreateUserMovie(AppUser user, Movie movie) {
        return userMovieRepository.findByUserAndMovie(user, movie)
                .orElseGet(() -> createUserMovie(user, movie));
    }

    private UserMovie createUserMovie(AppUser user, Movie movie, MovieStatus status) {
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(movie);
        userMovie.setStatus(status);
        return userMovieRepository.saveAndFlush(userMovie);
    }
    private UserMovie createUserMovie(AppUser user, Movie movie) {
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(movie);
        userMovie.setStatus(MovieStatus.UNWATCHED);
        return userMovieRepository.save(userMovie);
    }


    @jakarta.transaction.Transactional
    public void addPlannedMovie(AppUser user, Movie movie) {
        // Проверяем наличие фильма в базе по его IMDb ID, иначе сохраняем
        Movie existingMovie = movieRepository.findByImdbId(movie.getImdbId())
                .orElseGet(() -> movieRepository.save(movie));

        // Проверяем, что фильм еще не добавлен пользователем со статусом WANT_TO_WATCH
        Optional<UserMovie> existingUserMovie = userMovieRepository.findByUserAndMovieAndStatus(user, existingMovie, MovieStatus.WANT_TO_WATCH);
        if (existingUserMovie.isPresent()) {
            logger.info("Фильм уже добавлен пользователем: {}", user.getUsername());
            return;
        }

        // Создаем новую запись для добавления фильма пользователю
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(existingMovie);
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH);
        userMovieRepository.save(userMovie);

        logger.info("Фильм добавлен в запланированные для пользователя: {}", user.getUsername());
    }


    @Transactional
    public void addRating(String username, String imdbId, double rating) {
        AppUser user = appUserService.findByUsername(username);
        Movie movie = movieRepository.findByImdbId(imdbId)
                .orElseThrow(() -> new IllegalArgumentException("Фильм с таким ID не найден: " + imdbId));

        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WATCHED)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке просмотренных."));

        userMovie.setRating(rating);
        userMovieRepository.save(userMovie);
    }

    public double getAverageFriendRating(AppUser user, Movie movie) {
        // Получаем список друзей пользователя
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());

        // Получаем список оценок для фильма от друзей
        List<UserMovie> friendRatings = userMovieRepository.findByMovieAndUserIn(movie, friends);

        // Фильтруем null значения и вычисляем средний рейтинг
        return friendRatings.stream()
                .map(UserMovie::getRating)
                .filter(rating -> rating != null)  // Исключаем null значения
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);  // Значение по умолчанию, если нет оценок
    }

    public List<UserMovie> getWatchedMovies(AppUser user) {
        return userMovieRepository.findByUserAndStatus(user, MovieStatus.WATCHED);
    }


    public void removeWatchedMovie(AppUser user, Movie movie) {
        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WATCHED)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке просмотренных."));
        userMovieRepository.delete(userMovie);
    }

    public long getMovieCountByMovieId(Long movieId) {
        return userMovieRepository.countByMovieId(movieId);
    }

    public void updateMovieStatus(AppUser user, Movie movie, MovieStatus newStatus) {
        Optional<UserMovie> userMovieOpt = userMovieRepository.findByUserAndMovieAndStatus(user, movie, newStatus);
        if (userMovieOpt.isPresent()) {
            UserMovie userMovie = userMovieOpt.get();
            userMovie.setStatus(MovieStatus.UNWATCHED);
            userMovieRepository.save(userMovie);
        }
    }

    @Transactional
    public List<UserMovie> getPlannedMovies(AppUser user) {
        return userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH);
    }

    @Transactional
    public List<UserMovie> getFriendsPlannedMovies(AppUser currentUser) {
        return userMovieRepository.findPlannedMoviesByFriends(currentUser, MovieStatus.WANT_TO_WATCH);
    }

    @Transactional
    public void addHype(AppUser user, Movie movie, int hype) {
        UserMovie userMovie = userMovieRepository.findByUserAndMovie(user, movie)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден у пользователя"));
        userMovie.setHype(hype);
        userMovieRepository.save(userMovie);
    }
    public double getAverageFriendHype(AppUser user, Movie movie) {
        List<UserMovie> friendsHype = userMovieRepository.findAllByMovieAndFriends(user.getUsername(), movie.getImdbId());

        // Логирование для проверки друзей, оценивших фильм
        logger.info("Пользователь: {}", user.getUsername());
        logger.info("Фильм: {}", movie.getTitle());
        logger.info("Количество друзей, оценивших фильм ажиотажем: {}", friendsHype.size());

        // Фильтруем записи, где ажиотаж не равен null, и суммируем
        int totalHype = friendsHype.stream()
                .filter(friendMovie -> friendMovie.getHype() != null)
                .peek(friendMovie -> logger.info("Друг: {}, Ажиотаж: {}", friendMovie.getUser().getUsername(), friendMovie.getHype()))
                .mapToInt(UserMovie::getHype)
                .sum();

        // Количество друзей, которые выставили ажиотаж
        long count = friendsHype.stream()
                .filter(friendMovie -> friendMovie.getHype() != null)
                .count();

        // Логирование для итогового результата
        double averageHype = count > 0 ? (double) totalHype / count : 0.0;
        logger.info("Общий ажиотаж: {}, Средний ажиотаж: {}", totalHype, averageHype);

        return averageHype;
    }

    public List<UserMovie> getCombinedPlannedMovies(AppUser user) {
        // Получаем свои запланированные фильмы
        List<UserMovie> userPlannedMovies = userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH);

        // Получаем фильмы, предложенные друзьями
        List<UserMovie> friendsSuggestedMovies = userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND);

        // Объединяем фильмы
        List<UserMovie> combinedMovies = new ArrayList<>(userPlannedMovies);
        combinedMovies.addAll(friendsSuggestedMovies);

        return combinedMovies;
    }

    public void addSuggestedMovie(AppUser friend, Movie movie, String suggestedByUsername) {
        UserMovie suggestedMovie = new UserMovie();
        suggestedMovie.setUser(friend);
        suggestedMovie.setMovie(movie);
        suggestedMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        suggestedMovie.setSuggestedBy(suggestedByUsername);
        userMovieRepository.save(suggestedMovie);
    }

    @Transactional
    public void removePlannedMovieAndUpdateFriends(AppUser user, Movie movie) {
        // Находим запланированный фильм у пользователя
        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WANT_TO_WATCH)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке запланированных."));

        // Меняем его статус на UNWATCHED для самого пользователя
        userMovie.setStatus(MovieStatus.UNWATCHED);
        userMovieRepository.save(userMovie);

        // Получаем всех друзей пользователя
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());

        // Обновляем статус предложенного фильма на UNWATCHED у друзей
        for (AppUser friend : friends) {
            userMovieRepository.findByUserAndMovieAndStatus(friend, movie, MovieStatus.WANT_TO_WATCH_BY_FRIEND)
                    .ifPresent(friendMovie -> {
                        friendMovie.setStatus(MovieStatus.UNWATCHED);
                        userMovieRepository.save(friendMovie);
                    });
        }

        logger.info("Фильм удален из запланированных для пользователя {} и обновлен статус у друзей.", user.getUsername());
    }

    @Transactional
    public void updateSuggestedMoviesStatus(AppUser user, AppUser friend, MovieStatus newStatus) {
        List<UserMovie> suggestedMovies = userMovieRepository.findByUserAndStatusAndSuggestedBy(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND, friend.getUsername());

        for (UserMovie userMovie : suggestedMovies) {
            userMovie.setStatus(newStatus);
            userMovieRepository.save(userMovie);
        }

        logger.info("Статус предложенных фильмов от {} для {} обновлен на {}", friend.getUsername(), user.getUsername(), newStatus);
    }

    public boolean isMovieOwner(AppUser user, Movie movie) {
        Optional<UserMovie> userMovieOpt = userMovieRepository.findByUserAndMovie(user, movie);
        return userMovieOpt.isPresent() && userMovieOpt.get().getStatus() == MovieStatus.WANT_TO_WATCH;
    }

    public Optional<UserMovie> findByUserAndMovieAndStatus(AppUser user, Movie movie, MovieStatus status) {
        return userMovieRepository.findByUserAndMovieAndStatus(user, movie, status);
    }

    @Transactional
    public List<Movie> getWatchedMoviesByUser(Long userId) {
        return userMovieRepository.findByUserIdAndStatus(userId, MovieStatus.WATCHED)
                .stream()
                .map(UserMovie::getMovie)
                .collect(Collectors.toList());
    }



}
