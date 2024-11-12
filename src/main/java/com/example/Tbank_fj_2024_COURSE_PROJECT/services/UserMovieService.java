package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.UserMovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserMovieService {

    private static final Logger logger = LoggerFactory.getLogger(UserMovieService.class);

    private final AppUserService appUserService;
    private final MovieService movieService;
    private final UserMovieRepository userMovieRepository;
    private final FriendshipService friendshipService;
    private final MessageSender messageSender;

    @Autowired
    public UserMovieService(AppUserService appUserService, MovieService movieService,
                            UserMovieRepository userMovieRepository,@Lazy FriendshipService friendshipService,
                            MessageSender messageSender) {
        this.appUserService = appUserService;
        this.movieService = movieService;
        this.userMovieRepository = userMovieRepository;
        this.friendshipService = friendshipService;
        this.messageSender = messageSender;
    }

    @Transactional
    public void addWatchedMovie(AppUser user, Movie movie, String chatId) {
        Optional<UserMovie> existingMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WANT_TO_WATCH);

        UserMovie userMovie;
        if (existingMovie.isPresent()) {
            userMovie = existingMovie.get();
            updateMovieStatus(userMovie, MovieStatus.WATCHED);
        } else {
            userMovie = findOrCreateUserMovie(user, movie, MovieStatus.UNWATCHED);
            if (userMovie.getStatus() == MovieStatus.WATCHED) {
                messageSender.sendMessage(chatId, "Фильм \"" + movie.getTitle() + "\" уже добавлен в ваш список просмотренных.");
                return;
            }
            updateMovieStatus(userMovie, MovieStatus.WATCHED);
        }

        // Обновляем статус фильма на UNWATCHED у всех друзей, если он был предложен
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());
        for (AppUser friend : friends) {
            updateFriendMovieStatus(friend, movie, MovieStatus.UNWATCHED);
        }

        messageSender.sendMessage(chatId, "Фильм \"" + movie.getTitle() + "\" добавлен в просмотренные.");
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
        return userMovieRepository.save(userMovie);
    }

    private void updateMovieStatus(UserMovie userMovie, MovieStatus newStatus) {
        userMovie.setStatus(newStatus);
        userMovieRepository.save(userMovie);
    }


    private void updateFriendMovieStatus(AppUser friend, Movie movie, MovieStatus status) {
        userMovieRepository.findByUserAndMovieAndStatus(friend, movie, MovieStatus.WANT_TO_WATCH_BY_FRIEND)
                .ifPresent(friendMovie -> {
                    friendMovie.setStatus(status);
                    userMovieRepository.save(friendMovie);
                });
    }

    public void setMovieStatusForUser(AppUser user, Movie movie, MovieStatus newStatus) {
        Optional<UserMovie> userMovieOpt = userMovieRepository.findByUserAndMovie(user, movie);
        if (userMovieOpt.isPresent()) {
            updateMovieStatus(userMovieOpt.get(), newStatus);
        } else {
            throw new IllegalArgumentException("Фильм не найден у пользователя.");
        }
    }
    @Transactional
    public void addPlannedMovie(AppUser user, Movie movie) {
        Movie existingMovieInstance = movieService.findOrSaveMovieByImdbId(movie.getImdbId(), movie);

        // Проверка, если фильм уже предложен другом
        if (userMovieRepository.findByUserAndMovieAndStatus(user, existingMovieInstance, MovieStatus.WANT_TO_WATCH_BY_FRIEND).isPresent()) {
            logger.info("Фильм уже предложен пользователю {} другом и находится в предложенных.", user.getUsername());
            messageSender.sendMessage(user.getTelegramId(), "Фильм уже предложен вашим другом. Вы можете добавить его в просмотренные.");
            return;
        }

        // Проверка, если фильм уже в запланированных
        if (userMovieRepository.findByUserAndMovieAndStatus(user, existingMovieInstance, MovieStatus.WANT_TO_WATCH).isPresent()) {
            logger.info("Фильм уже добавлен пользователем: {}", user.getUsername());
            return;
        }

        // Если фильм не предложен и не запланирован, добавляем его в запланированные
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(existingMovieInstance);
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH);
        userMovieRepository.save(userMovie);
        logger.info("Фильм добавлен в запланированные для пользователя: {}", user.getUsername());

        // Предлагаем фильм друзьям пользователя
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());
        for (AppUser friend : friends) {
            addSuggestedMovie(friend, existingMovieInstance, user.getUsername());
        }
    }


    @Transactional
    public void addRating(String username, String imdbId, double rating) {
        AppUser user = appUserService.findByUsername(username);
        Movie movie = movieService.getMovieByImdbId(imdbId);

        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WATCHED)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке просмотренных."));

        userMovie.setRating(rating);
        userMovieRepository.save(userMovie);
    }

    public double getAverageFriendRating(AppUser user, Movie movie) {
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());
        List<UserMovie> friendRatings = userMovieRepository.findByMovieAndUserIn(movie, friends);

        return friendRatings.stream()
                .map(UserMovie::getRating)
                .filter(rating -> rating != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public List<UserMovie> getWatchedMovies(AppUser user) {
        return userMovieRepository.findByUserAndStatus(user, MovieStatus.WATCHED);
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

        int totalHype = friendsHype.stream()
                .filter(friendMovie -> friendMovie.getHype() != null)
                .mapToInt(UserMovie::getHype)
                .sum();

        long count = friendsHype.stream()
                .filter(friendMovie -> friendMovie.getHype() != null)
                .count();

        return count > 0 ? (double) totalHype / count : 0.0;
    }

    public List<UserMovie> getCombinedPlannedMovies(AppUser user) {
        List<UserMovie> userPlannedMovies = userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH);
        List<UserMovie> friendsSuggestedMovies = userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND);

        List<UserMovie> combinedMovies = new ArrayList<>(userPlannedMovies);
        combinedMovies.addAll(friendsSuggestedMovies);

        return combinedMovies;
    }

    public void addSuggestedMovie(AppUser friend, Movie movie, String suggestedByUsername) {
        if (userMovieRepository.findByUserAndMovieAndStatus(friend, movie, MovieStatus.WANT_TO_WATCH_BY_FRIEND).isPresent()) {
            logger.info("Фильм уже предложен пользователю {} другом {}", friend.getUsername(), suggestedByUsername);
            return;
        }

        UserMovie suggestedMovie = new UserMovie();
        suggestedMovie.setUser(friend);
        suggestedMovie.setMovie(movie);
        suggestedMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        suggestedMovie.setSuggestedBy(suggestedByUsername);
        userMovieRepository.save(suggestedMovie);
    }

    @Transactional
    public void removePlannedMovieAndUpdateFriends(AppUser user, Movie movie) {
        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WANT_TO_WATCH)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке запланированных."));
        userMovie.setStatus(MovieStatus.UNWATCHED);
        userMovieRepository.save(userMovie);

        friendshipService.getFriends(user.getUsername()).forEach(friend -> updateFriendMovieStatus(friend, movie, MovieStatus.UNWATCHED));
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

    @Transactional
    public void removeWatchedMovie(AppUser user, Movie movie) {
        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WATCHED)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке просмотренных."));
        userMovie.setStatus(MovieStatus.UNWATCHED);
        userMovieRepository.save(userMovie);
    }
}
