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
                            UserMovieRepository userMovieRepository, @Lazy FriendshipService friendshipService,
                            MessageSender messageSender) {
        this.appUserService = appUserService;
        this.movieService = movieService;
        this.userMovieRepository = userMovieRepository;
        this.friendshipService = friendshipService;
        this.messageSender = messageSender;
    }


    // Создаёт запись фильма для пользователя с указанным статусом
    private UserMovie createUserMovie(AppUser user, Movie movie, MovieStatus status) {
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(movie);
        userMovie.setStatus(status);
        return userMovieRepository.save(userMovie);
    }

    // Обновляет статус фильма у пользователя
    private void updateMovieStatus(UserMovie userMovie, MovieStatus newStatus) {
        userMovie.setStatus(newStatus);
        userMovieRepository.save(userMovie);
    }

    // Устанавливает статус фильма для пользователя на UNWATCHED, используется для удаления
    public void setMovieStatusForUserToUnwatched(AppUser user, Movie movie) {
        Optional<UserMovie> userMovieOpt = userMovieRepository.findByUserAndMovie(user, movie);
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());
        if (userMovieOpt.isPresent()) {
            updateMovieStatus(userMovieOpt.get(), MovieStatus.UNWATCHED);
            friends.forEach(friend -> {
                Optional<UserMovie> friendMovieOpt = userMovieRepository.findByUserAndMovieAndStatus(friend, movie,
                        MovieStatus.WANT_TO_WATCH);
                if (friendMovieOpt.isPresent()) {
                    addSuggestedMovie(user, movie, friend.getUsername());
                }
            });
        } else {
            throw new IllegalArgumentException("Фильм не найден у пользователя.");
        }
    }

    // Добавляет фильм в просмотренные для пользователя
    @Transactional
    public void addWatchedMovie(AppUser user, Movie movie, String chatId) {
        Optional<UserMovie> existingMovie = userMovieRepository.findByUserAndMovie(user, movie);

        if (existingMovie.isPresent()) {
            UserMovie userMovie = existingMovie.get();

            if (userMovie.getStatus() == MovieStatus.WATCHED) {
                messageSender.sendMessage(chatId, "Фильм \"" + movie.getTitle() + "\" уже добавлен в ваш список просмотренных.");
                return;
            }
            updateMovieStatus(userMovie, MovieStatus.WATCHED);
        } else {
            createUserMovie(user, movie, MovieStatus.WATCHED);
            logger.info("Фильм добавлен в просмотренные для пользователя: {}", user.getUsername());
        }

        updateFriendsMovieStatusToUnwatched(user,movie);
        messageSender.sendMessage(chatId, "Фильм \"" + movie.getTitle() + "\" добавлен в просмотренные.");
    }

    // Добавляет фильм в запланированные для пользователя
    @Transactional
    public void addPlannedMovie(AppUser user, Movie movie) {
        Movie existingMovieInstance = movieService.findOrSaveMovieByImdbId(movie.getImdbId(), movie);
        Optional<UserMovie> existingUserMovie = findByUserAndMovie(user, existingMovieInstance);

        if (existingUserMovie.isPresent()) {
            UserMovie userMovie = existingUserMovie.get();

            if (userMovie.getStatus() == MovieStatus.WANT_TO_WATCH) {
                logger.info("Фильм уже запланирован для пользователя: {}", user.getUsername());
                return;
            } else {
                userMovie.setStatus(MovieStatus.WANT_TO_WATCH);
                userMovie.setSuggestedBy(null);
                saveUserMovie(userMovie);
                logger.info("Фильм у пользователя {} обновлён с UNWATCHED на WANT_TO_WATCH.", user.getUsername());
                suggestMovieToFriends(user, existingMovieInstance);
                return;
            }
        }
        UserMovie newUserMovie = createUserMovie(user,existingMovieInstance,MovieStatus.WANT_TO_WATCH);
        saveUserMovie(newUserMovie);
        logger.info("Фильм добавлен в запланированные для пользователя: {}", user.getUsername());

        suggestMovieToFriends(user, existingMovieInstance);
    }

    // Возвращает список всех запланированных фильмов (свои и предложенные друзьями)
    public List<UserMovie> getCombinedPlannedMovies(AppUser user) {
        List<UserMovie> userPlannedMovies = userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH);
        List<UserMovie> friendsSuggestedMovies = userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND);

        List<UserMovie> combinedMovies = new ArrayList<>(userPlannedMovies);
        combinedMovies.addAll(friendsSuggestedMovies);

        return combinedMovies;
    }

    // Добавляет оценку к фильму
    @Transactional
    public void addRating(String username, String imdbId, double rating) {
        AppUser user = appUserService.findByUsername(username);
        Movie movie = movieService.getMovieByImdbId(imdbId);

        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WATCHED)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке просмотренных."));

        userMovie.setRating(rating);
        userMovieRepository.save(userMovie);
    }


    // Рассчитывает среднюю оценку друзей для фильма
    public double getAverageFriendRating(AppUser user, Movie movie) {
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());
        friends.add(user);
        List<UserMovie> friendRatings = userMovieRepository.findByMovieAndUserIn(movie, friends);

        return friendRatings.stream()
                .map(UserMovie::getRating)
                .filter(rating -> rating != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    // Добавляет уровень ажиотажа (hype) для фильма
    @Transactional
    public void addHype(AppUser user, Movie movie, int hype) {
        UserMovie userMovie = userMovieRepository.findByUserAndMovie(user, movie)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден у пользователя"));
        userMovie.setHype(hype);
        userMovieRepository.save(userMovie);
    }


    // Рассчитывает средний уровень ажиотажа среди друзей
    public double getAverageFriendHype(AppUser user, Movie movie) {
        List<AppUser> friends = new ArrayList<>(friendshipService.getFriends(user.getUsername()));
        friends.add(user); // Добавляем самого пользователя

        List<MovieStatus> statuses = List.of(MovieStatus.WANT_TO_WATCH, MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        List<UserMovie> friendsHype = userMovieRepository.findAllByMovieAndUserInAndStatusIn(movie, friends, statuses);

        int totalHype = friendsHype.stream()
                .filter(friendMovie -> friendMovie.getHype() != null)
                .mapToInt(UserMovie::getHype)
                .sum();

        long count = friendsHype.stream()
                .filter(friendMovie -> friendMovie.getHype() != null)
                .count();

        return count > 0 ? (double) totalHype / count : 0.0;
    }

    // Добавляет фильм в список предложенных другу
    public void addSuggestedMovie(AppUser friend, Movie movie, String suggestedByUsername) {
        Optional<UserMovie> existingSuggestion = findByUserAndMovie(friend, movie);

        if (existingSuggestion.isPresent()) {
            UserMovie friendMovie = existingSuggestion.get();
            if (friendMovie.getStatus() == MovieStatus.WANT_TO_WATCH_BY_FRIEND &&
                    suggestedByUsername.equals(friendMovie.getSuggestedBy())) {
                logger.info("Фильм уже предложен пользователю {} от {}", friend.getUsername(), suggestedByUsername);
                return;
            }
            if (friendMovie.getStatus() == MovieStatus.UNWATCHED) {
                friendMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
                friendMovie.setSuggestedBy(suggestedByUsername);
                saveUserMovie(friendMovie);
                logger.info("Фильм обновлён как предложенный пользователю {} от {}", friend.getUsername(), suggestedByUsername);
                return;
            }
            if (friendMovie.getStatus() == MovieStatus.WANT_TO_WATCH) {
                logger.info("Фильм уже добавлен пользователем {} в WANT_TO_WATCH", friend.getUsername());
                return;
            }
        }
        UserMovie newUserMovie = createUserMovie(friend,movie,MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        newUserMovie.setSuggestedBy(suggestedByUsername);
        saveUserMovie(newUserMovie);
        logger.info("Фильм предложен пользователю {} от {}", friend.getUsername(), suggestedByUsername);
    }

    // Обновляет статус предложенных фильмов
    @Transactional
    public void updateSuggestedMoviesStatus(AppUser user, AppUser friend, MovieStatus newStatus) {
        List<UserMovie> suggestedMovies = userMovieRepository.findByUserAndStatusAndSuggestedBy(user,
                MovieStatus.WANT_TO_WATCH_BY_FRIEND, friend.getUsername());
        for (UserMovie userMovie : suggestedMovies) {
            if (newStatus == MovieStatus.UNWATCHED) {
                userMovie.setSuggestedBy(null);
            }
            userMovie.setStatus(newStatus);
            userMovieRepository.save(userMovie);
        }
        logger.info("Статус предложенных фильмов от {} для {} обновлен на {}", friend.getUsername(), user.getUsername(), newStatus);
    }

    // Меняет статус фильма на UNWATCHED
    @Transactional
    public void updateFriendsMovieStatusToUnwatched(AppUser user, Movie movie) {
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());
        List<UserMovie> friendsMovies = userMovieRepository.findAllByMovieAndUserInAndStatus(movie, friends,
                MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        for (UserMovie friendMovie : friendsMovies) {
            friendMovie.setStatus(MovieStatus.UNWATCHED);
        }
        userMovieRepository.saveAll(friendsMovies);
    }

    // Предлагает фильм всем друзьям пользователя
    private void suggestMovieToFriends(AppUser user, Movie movie) {
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());

        for (AppUser friend : friends) {
            addSuggestedMovie(friend, movie, user.getUsername());
        }
    }

    // Удаляет запланированный фильм и обновляет информацию у друзей
    @Transactional
    public void removePlannedMovieAndUpdateFriends(AppUser user, Movie movie) {
        UserMovie userMovie = userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WANT_TO_WATCH)
                .orElseThrow(() -> new IllegalArgumentException("Фильм не найден в списке запланированных."));
        userMovie.setStatus(MovieStatus.UNWATCHED);
        userMovie.setSuggestedBy(null);
        saveUserMovie(userMovie);
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());

        friends.forEach(friend -> {
            Optional<UserMovie> friendMovieOpt = userMovieRepository.findByUserAndMovie(friend, movie);
            if (friendMovieOpt.isPresent()) {
                UserMovie friendMovie = friendMovieOpt.get();

                if (friendMovie.getStatus() == MovieStatus.WANT_TO_WATCH_BY_FRIEND &&
                        user.getUsername().equals(friendMovie.getSuggestedBy())) {
                    friendMovie.setStatus(MovieStatus.UNWATCHED);
                    friendMovie.setSuggestedBy(null);
                    saveUserMovie(friendMovie);
                }
            }
        });

        friends.forEach(friend -> {
            Optional<UserMovie> friendMovieOpt = userMovieRepository.findByUserAndMovieAndStatus(friend, movie, MovieStatus.WANT_TO_WATCH);
            if (friendMovieOpt.isPresent()) {
                addSuggestedMovie(user, movie, friend.getUsername());
            }
        });
        logger.info("Фильм удалён из списка запланированных пользователя {} и обновлён у друзей.", user.getUsername());
    }

    // Проверяет, является ли пользователь владельцем фильма
    public boolean isMovieOwner(AppUser user, Movie movie) {
        Optional<UserMovie> userMovieOpt = userMovieRepository.findByUserAndMovie(user, movie);
        return userMovieOpt.isPresent() && userMovieOpt.get().getStatus() == MovieStatus.WANT_TO_WATCH;
    }

    // Возвращает список просмотренных фильмов для пользователя
    public List<UserMovie> getUserMoviesWithDetails(AppUser user) {
        return userMovieRepository.findByUserAndStatus(user, MovieStatus.WATCHED);
    }

    // Возвращает список просмотренных фильмов по идентификатору пользователя
    @Transactional
    public List<Movie> getWatchedMoviesByUserId(Long userId) {
        return userMovieRepository.findByUserIdAndStatus(userId, MovieStatus.WATCHED)
                .stream()
                .map(UserMovie::getMovie)
                .collect(Collectors.toList());
    }

    // Возвращает список запланированных фильмов для пользователя
    public List<UserMovie> getPlannedMoviesForUser(AppUser user) {
        return userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH);
    }

    // Сохраняет или обновляет фильм пользователя
    public UserMovie saveUserMovie(UserMovie userMovie) {
        return userMovieRepository.save(userMovie);
    }

    // Ищет фильм пользователя
    public Optional<UserMovie> findByUserAndMovie(AppUser user, Movie movie) {
        return userMovieRepository.findByUserAndMovie(user, movie);
    }

}