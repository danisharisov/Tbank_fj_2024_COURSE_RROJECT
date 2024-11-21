package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.Friendship;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.FriendshipStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.FriendshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipService.class);

    private final FriendshipRepository friendshipRepository;
    private final AppUserService appUserService;
    private final UserMovieService userMovieService;

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository,
                             AppUserService appUserService,
                             UserMovieService userMovieService) {
        this.friendshipRepository = friendshipRepository;
        this.appUserService = appUserService;
        this.userMovieService = userMovieService;
    }

    public void addFriendRequest(String currentUsername, String friendUsername) {
        validateNotSelf(currentUsername, friendUsername);

        AppUser currentUser = appUserService.findByUsername(currentUsername);
        AppUser friendUser = appUserService.findByUsername(friendUsername);

        if (friendshipExists(currentUser, friendUser)) {
            throw new IllegalArgumentException("Запрос на дружбу уже отправлен или вы уже друзья.");
        }

        friendshipRepository.save(createFriendship(currentUser, friendUser, FriendshipStatus.PENDING));
        logger.info("Запрос на дружбу отправлен от {} к {}", currentUsername, friendUsername);
    }

    private void validateNotSelf(String currentUsername, String friendUsername) {
        if (currentUsername.equals(friendUsername)) {
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья.");
        }
    }

    private boolean friendshipExists(AppUser user, AppUser friend) {
        return friendshipRepository.findByUserAndFriend(user, friend).isPresent();
    }

    private Friendship createFriendship(AppUser user, AppUser friend, FriendshipStatus status) {
        Friendship friendship = new Friendship();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setStatus(status);
        return friendship;
    }

    public void rejectFriendRequest(String currentUsername, String requesterUsername) {
        AppUser currentUser = appUserService.findByUsername(currentUsername);
        AppUser requesterUser = appUserService.findByUsername(requesterUsername);

        Friendship friendship = friendshipRepository.findByUserAndFriend(requesterUser, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Заявка на дружбу не найдена."));

        friendshipRepository.delete(friendship);
        logger.info("Запрос на дружбу от {} к {} отклонен", requesterUsername, currentUsername);
    }

    public void acceptFriendRequest(String currentUsername, String requesterUsername) {
        AppUser currentUser = appUserService.findByUsername(currentUsername);
        AppUser requesterUser = appUserService.findByUsername(requesterUsername);

        Friendship friendship = friendshipRepository.findByUserAndFriend(requesterUser, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Заявка на дружбу не найдена."));

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        // Синхронизируем фильмы текущего пользователя с запросившим
        synchronizePlannedMovies(currentUser, requesterUser);

        // Синхронизируем фильмы запросившего с текущим
        synchronizePlannedMovies(requesterUser, currentUser);

        logger.info("Запрос на дружбу от {} к {} принят", requesterUsername, currentUsername);
    }

    @Transactional
    public List<AppUser> getFriends(String username) {
        AppUser user = appUserService.findByUsername(username);

        return friendshipRepository.findAllByUserOrFriendAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(friendship -> friendship.getUser().equals(user) ? friendship.getFriend() : friendship.getUser())
                .collect(Collectors.toList());
    }

    public List<AppUser> getIncomingRequests(String username) {
        AppUser user = appUserService.findByUsername(username);
        return friendshipRepository.findAllByFriendAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(Friendship::getUser)
                .collect(Collectors.toList());
    }

    public List<AppUser> getOutgoingRequests(String username) {
        AppUser user = appUserService.findByUsername(username);
        return friendshipRepository.findAllByUserAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(Friendship::getFriend)
                .collect(Collectors.toList());
    }

    public void removeFriendship(String currentUsername, String friendUsername) {
        AppUser currentUser = appUserService.findByUsername(currentUsername);
        AppUser friendUser = appUserService.findByUsername(friendUsername);

        deleteFriendship(currentUser, friendUser);
        deleteFriendship(friendUser, currentUser);

        userMovieService.updateSuggestedMoviesStatus(currentUser, friendUser, MovieStatus.UNWATCHED);
        userMovieService.updateSuggestedMoviesStatus(friendUser, currentUser, MovieStatus.UNWATCHED);
        logger.info("Дружба между {} и {} успешно удалена", currentUsername, friendUsername);
    }

    private void deleteFriendship(AppUser user, AppUser friend) {
        friendshipRepository.findByUserAndFriend(user, friend)
                .ifPresent(friendshipRepository::delete);
    }

    public void cancelFriendRequest(String senderUsername, String receiverUsername) {
        AppUser sender = appUserService.findByUsername(senderUsername);
        AppUser receiver = appUserService.findByUsername(receiverUsername);

        friendshipRepository.findOutgoingRequest(sender, receiver)
                .ifPresent(friendshipRepository::delete);

        logger.info("Запрос на добавление в друзья от {} к {} был отменен.", senderUsername, receiverUsername);
    }

    public void synchronizePlannedMovies(AppUser sourceUser, AppUser targetUser) {
        List<UserMovie> plannedMovies = userMovieService.getPlannedMoviesForUser(sourceUser);

        for (UserMovie userMovie : plannedMovies) {
            Movie movie = userMovie.getMovie();

            Optional<UserMovie> existingMovie = userMovieService.findByUserAndMovie(targetUser, movie);

            if (existingMovie.isPresent()) {
                UserMovie targetUserMovie = existingMovie.get();

                // Если статус уже WANT_TO_WATCH_BY_FRIEND, предложенный источником, пропускаем
                if (targetUserMovie.getStatus() == MovieStatus.WANT_TO_WATCH_BY_FRIEND ||
                        targetUserMovie.getStatus() == MovieStatus.WANT_TO_WATCH ||
                        targetUserMovie.getStatus() == MovieStatus.WATCHED) {
                    continue;
                }

                // Если статус UNWATCHED, обновляем его на WANT_TO_WATCH_BY_FRIEND
                if (targetUserMovie.getStatus() == MovieStatus.UNWATCHED) {
                    targetUserMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
                    targetUserMovie.setSuggestedBy(sourceUser.getUsername());
                    userMovieService.saveUserMovie(targetUserMovie);
                    logger.info("Фильм обновлён для пользователя {}: статус UNWATCHED -> WANT_TO_WATCH_BY_FRIEND", targetUser.getUsername());
                    continue;
                }
            }

            // Добавляем фильм как предложенный
            userMovieService.addSuggestedMovie(targetUser, movie, sourceUser.getUsername());
        }
    }




}
