package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.Friendship;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.FriendshipStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.FriendshipRepository;
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

    // Тестируем успешное добавление заявки в друзья
    @Test
    void addFriendRequest_Success() {
        String currentUsername = "user1";
        String friendUsername = "user2";

        AppUser currentUser = new AppUser();
        currentUser.setUsername(currentUsername);

        AppUser friendUser = new AppUser();
        friendUser.setUsername(friendUsername);

        when(appUserService.findByUsername(currentUsername)).thenReturn(currentUser);
        when(appUserService.findByUsername(friendUsername)).thenReturn(friendUser);
        when(friendshipRepository.findByUserAndFriend(currentUser, friendUser)).thenReturn(Optional.empty());

        friendshipService.addFriendRequest(currentUsername, friendUsername);

        verify(friendshipRepository, times(1)).save(any(Friendship.class));
    }

    // Тестируем ошибку при добавлении самого себя в друзья
    @Test
    void addFriendRequest_SelfFriendRequest_ThrowsException() {
        String currentUsername = "user1";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> friendshipService.addFriendRequest(currentUsername, currentUsername));

        assertEquals("Нельзя добавить самого себя в друзья.", exception.getMessage());
    }

    // Тестируем успешное получение входящих заявок
    @Test
    void getIncomingRequests_Success() {
        String username = "user1";

        AppUser user = new AppUser();
        user.setUsername(username);

        AppUser requester = new AppUser();
        requester.setUsername("requester1");

        Friendship friendship = new Friendship();
        friendship.setUser(requester);
        friendship.setFriend(user);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(appUserService.findByUsername(username)).thenReturn(user);
        when(friendshipRepository.findAllByFriendAndStatus(user, FriendshipStatus.PENDING))
                .thenReturn(List.of(friendship));

        List<AppUser> incomingRequests = friendshipService.getIncomingRequests(username);

        assertEquals(1, incomingRequests.size());
        assertEquals("requester1", incomingRequests.get(0).getUsername());
    }

    // Тестируем успешное получение исходящих заявок
    @Test
    void getOutgoingRequests_Success() {
        String username = "user1";

        AppUser user = new AppUser();
        user.setUsername(username);

        AppUser receiver = new AppUser();
        receiver.setUsername("receiver1");

        Friendship friendship = new Friendship();
        friendship.setUser(user);
        friendship.setFriend(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(appUserService.findByUsername(username)).thenReturn(user);
        when(friendshipRepository.findAllByUserAndStatus(user, FriendshipStatus.PENDING))
                .thenReturn(List.of(friendship));

        List<AppUser> outgoingRequests = friendshipService.getOutgoingRequests(username);

        assertEquals(1, outgoingRequests.size());
        assertEquals("receiver1", outgoingRequests.get(0).getUsername());
    }

    // Тестируем успешное принятие заявки в друзья
    @Test
    void acceptFriendRequest_Success() {
        String currentUsername = "user2";
        String requesterUsername = "user1";

        AppUser currentUser = new AppUser();
        currentUser.setUsername(currentUsername);

        AppUser requesterUser = new AppUser();
        requesterUser.setUsername(requesterUsername);

        Friendship friendship = new Friendship();
        friendship.setUser(requesterUser);
        friendship.setFriend(currentUser);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(appUserService.findByUsername(currentUsername)).thenReturn(currentUser);
        when(appUserService.findByUsername(requesterUsername)).thenReturn(requesterUser);
        when(friendshipRepository.findByUserAndFriend(requesterUser, currentUser)).thenReturn(Optional.of(friendship));

        friendshipService.acceptFriendRequest(currentUsername, requesterUsername);

        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        verify(friendshipRepository, times(1)).save(friendship);
        verify(userMovieService, times(1)).getPlannedMoviesForUser(requesterUser);
        verify(userMovieService, times(1)).getPlannedMoviesForUser(currentUser);
    }

    // Тестируем успешное отклонение заявки в друзья
    @Test
    void rejectFriendRequest_Success() {
        String currentUsername = "user2";
        String requesterUsername = "user1";

        AppUser currentUser = new AppUser();
        currentUser.setUsername(currentUsername);

        AppUser requesterUser = new AppUser();
        requesterUser.setUsername(requesterUsername);

        Friendship friendship = new Friendship();
        friendship.setUser(requesterUser);
        friendship.setFriend(currentUser);

        when(appUserService.findByUsername(currentUsername)).thenReturn(currentUser);
        when(appUserService.findByUsername(requesterUsername)).thenReturn(requesterUser);
        when(friendshipRepository.findByUserAndFriend(requesterUser, currentUser)).thenReturn(Optional.of(friendship));

        friendshipService.rejectFriendRequest(currentUsername, requesterUsername);

        verify(friendshipRepository, times(1)).delete(friendship);
    }

    // Тестируем успешное получение списка друзей
    @Test
    void getFriends_Success() {
        String username = "user1";

        AppUser user = new AppUser();
        user.setUsername(username);

        AppUser friend = new AppUser();
        friend.setUsername("friend1");

        Friendship friendship = new Friendship();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setStatus(FriendshipStatus.ACCEPTED);

        when(appUserService.findByUsername(username)).thenReturn(user);
        when(friendshipRepository.findAllByUserOrFriendAndStatus(user, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(friendship));

        List<AppUser> friends = friendshipService.getFriends(username);

        assertEquals(1, friends.size());
        assertEquals("friend1", friends.get(0).getUsername());
    }

    // Тестируем сценарий: запрос уже существует
    @Test
    void addFriendRequest_RequestAlreadyExists_ThrowsException() {
        String currentUsername = "user1";
        String friendUsername = "user2";

        AppUser currentUser = new AppUser();
        currentUser.setUsername(currentUsername);

        AppUser friendUser = new AppUser();
        friendUser.setUsername(friendUsername);

        Friendship existingFriendship = new Friendship();
        existingFriendship.setUser(currentUser);
        existingFriendship.setFriend(friendUser);

        when(appUserService.findByUsername(currentUsername)).thenReturn(currentUser);
        when(appUserService.findByUsername(friendUsername)).thenReturn(friendUser);
        when(friendshipRepository.findByUserAndFriend(currentUser, friendUser)).thenReturn(Optional.of(existingFriendship));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> friendshipService.addFriendRequest(currentUsername, friendUsername));

        assertEquals("Запрос на дружбу уже отправлен или вы уже друзья.", exception.getMessage());
    }


    // Тестируем успешное удаление дружбы
    @Test
    void removeFriendship_Success() {
        String currentUsername = "user1";
        String friendUsername = "user2";

        AppUser currentUser = new AppUser();
        currentUser.setUsername(currentUsername);

        AppUser friendUser = new AppUser();
        friendUser.setUsername(friendUsername);

        Friendship friendship = new Friendship();
        friendship.setUser(currentUser);
        friendship.setFriend(friendUser);

        when(appUserService.findByUsername(currentUsername)).thenReturn(currentUser);
        when(appUserService.findByUsername(friendUsername)).thenReturn(friendUser);
        when(friendshipRepository.findByUserAndFriend(currentUser, friendUser)).thenReturn(Optional.of(friendship));

        friendshipService.removeFriendship(currentUsername, friendUsername);

        verify(friendshipRepository, times(1)).delete(friendship);
        verify(userMovieService, times(1))
                .updateSuggestedMoviesStatus(currentUser, friendUser, MovieStatus.UNWATCHED);
        verify(userMovieService, times(1))
                .updateSuggestedMoviesStatus(friendUser, currentUser, MovieStatus.UNWATCHED);
    }

    // Тестируем успешную синхронизацию фильмов
    @Test
    void synchronizePlannedMovies_Success() {
        AppUser sourceUser = new AppUser();
        sourceUser.setUsername("sourceUser");

        AppUser targetUser = new AppUser();
        targetUser.setUsername("targetUser");

        Movie movie = new Movie();
        movie.setId(1L);

        UserMovie userMovie = new UserMovie();
        userMovie.setMovie(movie);
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        when(userMovieService.getPlannedMoviesForUser(sourceUser)).thenReturn(List.of(userMovie));
        when(userMovieService.findByUserAndMovie(targetUser, movie)).thenReturn(Optional.empty());

        friendshipService.synchronizePlannedMovies(sourceUser, targetUser);

        verify(userMovieService, times(1)).addSuggestedMovie(targetUser, movie, "sourceUser");
    }

    // Тестируем сценарий: фильм уже есть, статус UNWATCHED
    @Test
    void synchronizePlannedMovies_ExistingMovie_Unwatched() {
        AppUser sourceUser = new AppUser();
        sourceUser.setUsername("sourceUser");

        AppUser targetUser = new AppUser();
        targetUser.setUsername("targetUser");

        Movie movie = new Movie();
        movie.setId(1L);

        UserMovie sourceUserMovie = new UserMovie();
        sourceUserMovie.setMovie(movie);
        sourceUserMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        UserMovie targetUserMovie = new UserMovie();
        targetUserMovie.setMovie(movie);
        targetUserMovie.setStatus(MovieStatus.UNWATCHED);

        when(userMovieService.getPlannedMoviesForUser(sourceUser)).thenReturn(List.of(sourceUserMovie));
        when(userMovieService.findByUserAndMovie(targetUser, movie)).thenReturn(Optional.of(targetUserMovie));

        friendshipService.synchronizePlannedMovies(sourceUser, targetUser);

        assertEquals(MovieStatus.WANT_TO_WATCH_BY_FRIEND, targetUserMovie.getStatus());
        assertEquals("sourceUser", targetUserMovie.getSuggestedBy());
        verify(userMovieService, times(1)).saveUserMovie(targetUserMovie);
    }



}
