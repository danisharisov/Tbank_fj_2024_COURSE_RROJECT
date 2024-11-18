package com.example.Tbank_fj_2024_COURSE_PROJECT.services;


import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.Friendship;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.FriendshipStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.FriendshipRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private AppUserService appUserService;

    @Mock
    private UserMovieService userMovieService;

    @InjectMocks
    private FriendshipService friendshipService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddFriendRequest() {
        AppUser currentUser = new AppUser();
        currentUser.setUsername("user1");

        AppUser friendUser = new AppUser();
        friendUser.setUsername("user2");

        when(appUserService.findByUsername("user1")).thenReturn(currentUser);
        when(appUserService.findByUsername("user2")).thenReturn(friendUser);
        when(friendshipRepository.findByUserAndFriend(currentUser, friendUser)).thenReturn(Optional.empty());

        friendshipService.addFriendRequest("user1", "user2");

        verify(friendshipRepository, times(1)).save(any(Friendship.class));
    }

    @Test
    void testAddFriendRequestAlreadyExists() {
        AppUser currentUser = new AppUser();
        currentUser.setUsername("user1");

        AppUser friendUser = new AppUser();
        friendUser.setUsername("user2");

        Friendship existingFriendship = new Friendship();
        existingFriendship.setUser(currentUser);
        existingFriendship.setFriend(friendUser);
        existingFriendship.setStatus(FriendshipStatus.PENDING);

        when(appUserService.findByUsername("user1")).thenReturn(currentUser);
        when(appUserService.findByUsername("user2")).thenReturn(friendUser);
        when(friendshipRepository.findByUserAndFriend(currentUser, friendUser)).thenReturn(Optional.of(existingFriendship));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                friendshipService.addFriendRequest("user1", "user2"));
        assertEquals("Запрос на дружбу уже отправлен или вы уже друзья.", exception.getMessage());
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void testRejectNonExistentFriendRequest() {
        AppUser currentUser = new AppUser();
        currentUser.setUsername("user1");

        AppUser requesterUser = new AppUser();
        requesterUser.setUsername("user2");

        when(appUserService.findByUsername("user1")).thenReturn(currentUser);
        when(appUserService.findByUsername("user2")).thenReturn(requesterUser);
        when(friendshipRepository.findByUserAndFriend(requesterUser, currentUser)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                friendshipService.rejectFriendRequest("user1", "user2"));
        assertEquals("Заявка на дружбу не найдена.", exception.getMessage());
        verify(friendshipRepository, never()).delete(any(Friendship.class));
    }

    @Test
    void testAcceptNonExistentFriendRequest() {
        AppUser currentUser = new AppUser();
        currentUser.setUsername("user1");

        AppUser requesterUser = new AppUser();
        requesterUser.setUsername("user2");

        when(appUserService.findByUsername("user1")).thenReturn(currentUser);
        when(appUserService.findByUsername("user2")).thenReturn(requesterUser);
        when(friendshipRepository.findByUserAndFriend(requesterUser, currentUser)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                friendshipService.acceptFriendRequest("user1", "user2"));
        assertEquals("Заявка на дружбу не найдена.", exception.getMessage());
        verify(friendshipRepository, never()).save(any(Friendship.class));
    }

    @Test
    void testRemoveFriendship() {
        AppUser currentUser = new AppUser();
        currentUser.setUsername("user1");

        AppUser friendUser = new AppUser();
        friendUser.setUsername("user2");

        Friendship friendship1 = new Friendship();
        friendship1.setUser(currentUser);
        friendship1.setFriend(friendUser);

        Friendship friendship2 = new Friendship();
        friendship2.setUser(friendUser);
        friendship2.setFriend(currentUser);

        when(appUserService.findByUsername("user1")).thenReturn(currentUser);
        when(appUserService.findByUsername("user2")).thenReturn(friendUser);
        when(friendshipRepository.findByUserAndFriend(currentUser, friendUser)).thenReturn(Optional.of(friendship1));
        when(friendshipRepository.findByUserAndFriend(friendUser, currentUser)).thenReturn(Optional.of(friendship2));

        friendshipService.removeFriendship("user1", "user2");

        verify(friendshipRepository, times(1)).delete(friendship1);
        verify(friendshipRepository, times(1)).delete(friendship2);
        verify(userMovieService, times(1)).updateSuggestedMoviesStatus(currentUser, friendUser, MovieStatus.UNWATCHED);
        verify(userMovieService, times(1)).updateSuggestedMoviesStatus(friendUser, currentUser, MovieStatus.UNWATCHED);
    }

    @Test
    void testCancelNonExistentFriendRequest() {
        AppUser sender = new AppUser();
        sender.setUsername("user1");

        AppUser receiver = new AppUser();
        receiver.setUsername("user2");

        when(appUserService.findByUsername("user1")).thenReturn(sender);
        when(appUserService.findByUsername("user2")).thenReturn(receiver);
        when(friendshipRepository.findOutgoingRequest(sender, receiver)).thenReturn(Optional.empty());

        friendshipService.cancelFriendRequest("user1", "user2");

        verify(friendshipRepository, never()).delete(any(Friendship.class));
    }
    @Test
    void testSynchronizePlannedMovies() {
        AppUser sourceUser = new AppUser();
        sourceUser.setUsername("sourceUser");

        AppUser targetUser = new AppUser();
        targetUser.setUsername("targetUser");

        Movie testMovie = new Movie();
        testMovie.setTitle("Test Movie");

        UserMovie sourceUserMovie = new UserMovie();
        sourceUserMovie.setMovie(testMovie);
        sourceUserMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        // Мокаем фильмы у исходного пользователя
        when(userMovieService.getPlannedMoviesForUser(sourceUser)).thenReturn(List.of(sourceUserMovie));

        // Настраиваем поведение для findByUserAndMovie, чтобы возвращать Optional.empty()
        when(userMovieService.findByUserAndMovie(targetUser, testMovie)).thenReturn(Optional.empty());

        // Вызов тестируемого метода
        friendshipService.synchronizePlannedMovies(sourceUser, targetUser);

        // Проверка, что фильм был предложен целевому пользователю
        verify(userMovieService, times(1)).addSuggestedMovie(targetUser, testMovie, sourceUser.getUsername());
    }


    @Test
    void testGetFriends() {
        AppUser currentUser = new AppUser();
        currentUser.setUsername("user1");

        AppUser friendUser = new AppUser();
        friendUser.setUsername("user2");

        Friendship friendship = new Friendship();
        friendship.setUser(currentUser);
        friendship.setFriend(friendUser);
        friendship.setStatus(FriendshipStatus.ACCEPTED);

        when(appUserService.findByUsername("user1")).thenReturn(currentUser);
        when(friendshipRepository.findAllByUserOrFriendAndStatus(currentUser, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(friendship));

        List<AppUser> friends = friendshipService.getFriends("user1");

        assertEquals(1, friends.size());
        assertEquals("user2", friends.get(0).getUsername());
    }
}
