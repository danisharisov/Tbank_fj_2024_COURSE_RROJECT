package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.Friendship;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.FriendshipStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.FriendshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipService.class);
    private final FriendshipRepository friendshipRepository;
    private final AppUserRepository appUserRepository;

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, AppUserRepository appUserRepository) {
        this.friendshipRepository = friendshipRepository;
        this.appUserRepository = appUserRepository;
    }

    public void addFriendRequest(String currentUsername, String friendUsername) {
        AppUser currentUser = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + currentUsername));
        AppUser friendUser = appUserRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new IllegalArgumentException("Друг не найден: " + friendUsername));

        if (friendshipRepository.findByUserAndFriend(currentUser, friendUser).isPresent()) {
            throw new IllegalArgumentException("Запрос на дружбу уже отправлен или вы уже друзья.");
        }

        Friendship friendship = new Friendship();
        friendship.setUser(currentUser);
        friendship.setFriend(friendUser);
        friendship.setStatus(FriendshipStatus.PENDING);  // Устанавливаем статус как "ожидающий"
        friendshipRepository.save(friendship);
    }

    public void confirmFriendRequest(String currentUsername, String friendUsername) {
        AppUser currentUser = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + currentUsername));
        AppUser friendUser = appUserRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new IllegalArgumentException("Друг не найден: " + friendUsername));

        Friendship friendship = friendshipRepository.findByUserAndFriend(friendUser, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Запрос на дружбу не найден."));

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        // Создание обратной связи (двусторонняя дружба)
        Friendship reverseFriendship = new Friendship();
        reverseFriendship.setUser(currentUser);
        reverseFriendship.setFriend(friendUser);
        reverseFriendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(reverseFriendship);
    }


    public List<AppUser> getFriends(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));
        return friendshipRepository.findAllByUser(user)
                .stream()
                .map(Friendship::getFriend)
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
                .map(Friendship::getFriend)  // Возвращаем пользователей, которым был отправлен запрос
                .collect(Collectors.toList());
    }

    public void rejectFriendRequest(String currentUsername, String requestorUsername) {
        AppUser currentUser = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + currentUsername));
        AppUser requestorUser = appUserRepository.findByUsername(requestorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь, отправивший запрос, не найден: " + requestorUsername));

        // Ищем запрос на дружбу со статусом PENDING
        Friendship friendship = friendshipRepository.findByUserAndFriend(requestorUser, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Запрос на дружбу от " + requestorUsername + " не найден."));

        if (friendship.getStatus() == FriendshipStatus.PENDING) {
            friendshipRepository.delete(friendship);
            logger.info("Запрос на дружбу от {} к {} был отклонен.", requestorUsername, currentUsername);
        } else {
            throw new IllegalArgumentException("Запрос на дружбу уже принят или отклонен.");
        }
    }

    public void removeFriend(String currentUsername, String friendUsername) {
        AppUser currentUser = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + currentUsername));
        AppUser friendUser = appUserRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new IllegalArgumentException("Друг не найден: " + friendUsername));

        Optional<Friendship> friendship = friendshipRepository.findByUserAndFriend(currentUser, friendUser);
        Optional<Friendship> reverseFriendship = friendshipRepository.findByUserAndFriend(friendUser, currentUser);

        if (friendship.isPresent()) {
            friendshipRepository.delete(friendship.get());
        } else {
            throw new IllegalArgumentException("Пользователь " + friendUsername + " не является вашим другом.");
        }

        // Удаляем также обратную дружбу
        reverseFriendship.ifPresent(friendshipRepository::delete);
    }
}
