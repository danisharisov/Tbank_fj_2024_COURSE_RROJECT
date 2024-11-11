package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.Friendship;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.FriendshipStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
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
    private final AppUserRepository appUserRepository;
    private final UserMovieService userMovieService;

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository,
                             AppUserRepository appUserRepository,
                             UserMovieService userMovieService) {
        this.friendshipRepository = friendshipRepository;
        this.appUserRepository = appUserRepository;
        this.userMovieService = userMovieService;
    }

    public void addFriendRequest(String currentUsername, String friendUsername) {
        // Проверка на добавление самого себя
        if (currentUsername.equals(friendUsername)) {
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья.");
        }

        AppUser currentUser = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + currentUsername));
        AppUser friendUser = appUserRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new IllegalArgumentException("Друг не найден: " + friendUsername));

        if (friendshipRepository.findByUserAndFriend(currentUser, friendUser).isPresent()) {
            throw new IllegalArgumentException("Запрос на дружбу уже отправлен или вы уже друзья.");
        }

        // Создаем новый запрос
        Friendship friendship = new Friendship();
        friendship.setUser(currentUser);
        friendship.setFriend(friendUser);
        friendship.setStatus(FriendshipStatus.PENDING);  // Статус "Ожидает подтверждения"
        friendshipRepository.save(friendship);
    }

    public void rejectFriendRequest(String currentUsername, String requesterUsername) {
        AppUser currentUser = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + currentUsername));
        AppUser requesterUser = appUserRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new IllegalArgumentException("Друг не найден: " + requesterUsername));

        Friendship friendship = friendshipRepository.findByUserAndFriend(requesterUser, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Заявка на дружбу не найдена."));

        // Удаляем запись о заявке
        friendshipRepository.delete(friendship);
    }

    public void acceptFriendRequest(String currentUsername, String requesterUsername) {
        AppUser currentUser = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + currentUsername));
        AppUser requesterUser = appUserRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new IllegalArgumentException("Друг не найден: " + requesterUsername));

        Friendship friendship = friendshipRepository.findByUserAndFriend(requesterUser, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Заявка на дружбу не найдена."));

        // Меняем статус на "Подтверждено"
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
        synchronizePlannedMovies(currentUser, requesterUser);
        synchronizePlannedMovies(requesterUser, currentUser);
    }


    @Transactional
    public List<AppUser> getFriends(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));

        return friendshipRepository.findAllByUserOrFriendAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(friendship -> friendship.getUser().equals(user) ? friendship.getFriend() : friendship.getUser())
                .collect(Collectors.toList());
    }

    public List<AppUser> getIncomingRequests(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));
        return friendshipRepository.findAllByFriendAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(Friendship::getUser)  // Возвращаем пользователей, которые отправили запрос текущему пользователю
                .collect(Collectors.toList());
    }

    public List<AppUser> getOutgoingRequests(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));
        return friendshipRepository.findAllByUserAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(Friendship::getFriend)  // Возвращаем пользователей, которым текущий пользователь отправил запрос
                .collect(Collectors.toList());
    }
    public void removeFriendship(String currentUsername, String friendUsername) {
        AppUser currentUser = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + currentUsername));
        AppUser friendUser = appUserRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new IllegalArgumentException("Друг не найден: " + friendUsername));

        // Удаление дружбы у текущего пользователя
        friendshipRepository.findByUserAndFriend(currentUser, friendUser)
                .ifPresent(friendshipRepository::delete);

        // Удаление дружбы у друга
        friendshipRepository.findByUserAndFriend(friendUser, currentUser)
                .ifPresent(friendshipRepository::delete);

        userMovieService.updateSuggestedMoviesStatus(currentUser, friendUser, MovieStatus.UNWATCHED);
        userMovieService.updateSuggestedMoviesStatus(friendUser, currentUser, MovieStatus.UNWATCHED);

        logger.info("Дружба между {} и {} успешно удалена.", currentUsername, friendUsername);
    }

    public void cancelFriendRequest(String senderUsername, String receiverUsername) {
        AppUser sender = appUserRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + senderUsername));
        AppUser receiver = appUserRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("Друг не найден: " + receiverUsername));

        friendshipRepository.findOutgoingRequest(sender, receiver)
                .ifPresent(friendshipRepository::delete);

        logger.info("Запрос на добавление в друзья от {} к {} был отменен.", senderUsername, receiverUsername);
    }

    private void synchronizePlannedMovies(AppUser targetUser, AppUser sourceUser) {
        List<UserMovie> sourcePlannedMovies = userMovieService.getPlannedMovies(sourceUser);

        for (UserMovie sourceMovie : sourcePlannedMovies) {
            Movie movie = sourceMovie.getMovie();

            Optional<UserMovie> existingUserMovie = userMovieService.findByUserAndMovieAndStatus(targetUser, movie, MovieStatus.WANT_TO_WATCH_BY_FRIEND);
            if (existingUserMovie.isEmpty()) {
                userMovieService.addSuggestedMovie(targetUser, movie, sourceUser.getUsername());
            }
        }

        logger.info("Синхронизация запланированных фильмов между пользователями {} и {} завершена.", targetUser.getUsername(), sourceUser.getUsername());
    }






}
