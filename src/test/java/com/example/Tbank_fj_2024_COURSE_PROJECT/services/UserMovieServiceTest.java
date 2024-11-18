package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.UserMovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.AppUserService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.FriendshipService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.MovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.UserMovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class UserMovieServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private MovieService movieService;

    @Mock
    private UserMovieRepository userMovieRepository;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private UserMovieService userMovieService;

    private AppUser testUser;
    private Movie testMovie;
    private AppUser friend1;
    private AppUser friend2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new AppUser();
        testUser.setUsername("testUser");

        testMovie = new Movie();
        testMovie.setTitle("Test Movie");

        friend1 = new AppUser();
        friend1.setUsername("friend1");

        friend2 = new AppUser();
        friend2.setUsername("friend2");

        // Дополнительно: настройка мока для необходимых методов
        when(appUserService.findByUsername(testUser.getUsername())).thenReturn(testUser);
        when(movieService.getMovieByImdbId(testMovie.getImdbId())).thenReturn(testMovie);
    }

    @Test
    void testAddWatchedMovie_NewMovie() {
        // Проверка добавления нового фильма в просмотренные
        when(userMovieRepository.findByUserAndMovie(testUser, testMovie)).thenReturn(Optional.empty());

        userMovieService.addWatchedMovie(testUser, testMovie, "testChatId");

        verify(userMovieRepository, times(1)).save(any(UserMovie.class));
        verify(messageSender).sendMessage("testChatId", "Фильм \"Test Movie\" добавлен в просмотренные.");
    }

    @Test
    void testAddWatchedMovie_ExistingMovieNotWatched() {
        // Проверка обновления статуса на "WATCHED" для уже существующего фильма
        UserMovie existingUserMovie = new UserMovie();
        existingUserMovie.setStatus(MovieStatus.WANT_TO_WATCH);
        when(userMovieRepository.findByUserAndMovie(testUser, testMovie)).thenReturn(Optional.of(existingUserMovie));

        userMovieService.addWatchedMovie(testUser, testMovie, "testChatId");

        assertEquals(MovieStatus.WATCHED, existingUserMovie.getStatus());
        verify(userMovieRepository, times(1)).save(existingUserMovie);
        verify(messageSender).sendMessage("testChatId", "Фильм \"Test Movie\" добавлен в просмотренные.");
    }

    @Test
    void testAddWatchedMovie_AlreadyWatchedMovie() {
        // Проверка, что сообщение отправляется, если фильм уже в просмотренных
        UserMovie existingUserMovie = new UserMovie();
        existingUserMovie.setStatus(MovieStatus.WATCHED);
        when(userMovieRepository.findByUserAndMovie(testUser, testMovie)).thenReturn(Optional.of(existingUserMovie));

        userMovieService.addWatchedMovie(testUser, testMovie, "testChatId");

        verify(userMovieRepository, never()).save(existingUserMovie);
        verify(messageSender).sendMessage("testChatId", "Фильм \"Test Movie\" уже добавлен в ваш список просмотренных.");
    }

    @Test
    void testAddPlannedMovie_NewMovie() {
        // Проверка добавления нового фильма в запланированные
        when(movieService.findOrSaveMovieByImdbId(anyString(), any(Movie.class))).thenReturn(testMovie);
        when(userMovieRepository.findByUserAndMovie(testUser, testMovie)).thenReturn(Optional.empty());
        when(friendshipService.getFriends(testUser.getUsername())).thenReturn(new ArrayList<>());

        userMovieService.addPlannedMovie(testUser, testMovie);

        verify(userMovieRepository, times(1)).save(any(UserMovie.class));
        verify(friendshipService, times(1)).getFriends(testUser.getUsername());
    }

    @Test
    void testSetMovieStatusForUser_ExistingMovie() {
        UserMovie userMovie = new UserMovie();
        when(userMovieRepository.findByUserAndMovie(testUser, testMovie)).thenReturn(Optional.of(userMovie));

        userMovieService.setMovieStatusForUser(testUser, testMovie, MovieStatus.WANT_TO_WATCH);

        assertEquals(MovieStatus.WANT_TO_WATCH, userMovie.getStatus());
        verify(userMovieRepository, times(1)).save(userMovie);
    }

    @Test
    void testSetMovieStatusForUser_MovieNotFound() {
        when(userMovieRepository.findByUserAndMovie(testUser, testMovie)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userMovieService.setMovieStatusForUser(testUser, testMovie, MovieStatus.WANT_TO_WATCH);
        });
    }

    @Test
    void testAddRating_ValidMovie() {
        UserMovie userMovie = new UserMovie();
        userMovie.setStatus(MovieStatus.WATCHED);
        when(appUserService.findByUsername(anyString())).thenReturn(testUser);
        when(movieService.getMovieByImdbId(anyString())).thenReturn(testMovie);
        when(userMovieRepository.findByUserAndMovieAndStatus(testUser, testMovie, MovieStatus.WATCHED)).thenReturn(Optional.of(userMovie));

        userMovieService.addRating("testUser", "tt1234567", 8.5);

        assertEquals(8.5, userMovie.getRating());
        verify(userMovieRepository, times(1)).save(userMovie);
    }

    @Test
    void testAddRating_MovieNotFound() {
        when(appUserService.findByUsername(anyString())).thenReturn(testUser);
        when(movieService.getMovieByImdbId(anyString())).thenReturn(testMovie);
        when(userMovieRepository.findByUserAndMovieAndStatus(testUser, testMovie, MovieStatus.WATCHED)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userMovieService.addRating("testUser", "tt1234567", 8.5);
        });
    }

    @Test
    void testGetAverageFriendRating() {
        UserMovie friendMovie1 = new UserMovie();
        friendMovie1.setRating(7.0);
        UserMovie friendMovie2 = new UserMovie();
        friendMovie2.setRating(8.0);

        List<UserMovie> friendRatings = List.of(friendMovie1, friendMovie2);
        when(friendshipService.getFriends(testUser.getUsername())).thenReturn(List.of(new AppUser()));
        when(userMovieRepository.findByMovieAndUserIn(testMovie, List.of(new AppUser()))).thenReturn(friendRatings);

        double averageRating = userMovieService.getAverageFriendRating(testUser, testMovie);

        assertEquals(7.5, averageRating);
    }


    @Test
    void testAddHype() {
        UserMovie userMovie = new UserMovie();
        when(userMovieRepository.findByUserAndMovie(testUser, testMovie)).thenReturn(Optional.of(userMovie));

        userMovieService.addHype(testUser, testMovie, 80);

        assertEquals(80, userMovie.getHype());
        verify(userMovieRepository, times(1)).save(userMovie);
    }


    @Test
    void testAddRating_ExistingWatchedMovie() {
        UserMovie userMovie = new UserMovie();
        userMovie.setStatus(MovieStatus.WATCHED);
        when(appUserService.findByUsername(testUser.getUsername())).thenReturn(testUser);
        when(movieService.getMovieByImdbId(testMovie.getImdbId())).thenReturn(testMovie);
        when(userMovieRepository.findByUserAndMovieAndStatus(testUser, testMovie, MovieStatus.WATCHED))
                .thenReturn(Optional.of(userMovie));

        userMovieService.addRating(testUser.getUsername(), testMovie.getImdbId(), 8.5);

        assertEquals(8.5, userMovie.getRating());
        verify(userMovieRepository, times(1)).save(userMovie);
    }

    @Test
    void testGetAverageFriendHype() {
        List<AppUser> friends = List.of(friend1, friend2);
        when(friendshipService.getFriends(testUser.getUsername())).thenReturn(friends);

        UserMovie friend1Movie = new UserMovie();
        friend1Movie.setHype(60);

        UserMovie friend2Movie = new UserMovie();
        friend2Movie.setHype(80);

        UserMovie userMovie = new UserMovie();
        userMovie.setHype(100);

        when(userMovieRepository.findAllByMovieAndUserInAndStatusIn(eq(testMovie), anyList(), anyList()))
                .thenReturn(List.of(friend1Movie, friend2Movie, userMovie));

        double averageHype = userMovieService.getAverageFriendHype(testUser, testMovie);

        assertEquals(80.0, averageHype, 0.01, "Average hype should include the current user's hype");
    }



    @Test
    void testUpdateSuggestedMoviesStatus() {
        AppUser user = new AppUser();
        AppUser friend = new AppUser();
        user.setUsername("user1");
        friend.setUsername("friend1");

        UserMovie suggestedMovie = new UserMovie();
        suggestedMovie.setUser(user);
        suggestedMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        suggestedMovie.setSuggestedBy("friend1");

        when(userMovieRepository.findByUserAndStatusAndSuggestedBy(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND, friend.getUsername()))
                .thenReturn(List.of(suggestedMovie));

        userMovieService.updateSuggestedMoviesStatus(user, friend, MovieStatus.UNWATCHED);

        assertEquals(MovieStatus.UNWATCHED, suggestedMovie.getStatus());
        verify(userMovieRepository, times(1)).save(suggestedMovie);
    }

    @Test
    void testAddPlannedMovie_UpdateExistingStatus() {
        AppUser user = new AppUser();
        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie existingUserMovie = new UserMovie();
        existingUserMovie.setStatus(MovieStatus.UNWATCHED);

        when(movieService.findOrSaveMovieByImdbId(movie.getImdbId(), movie)).thenReturn(movie);
        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.of(existingUserMovie));

        userMovieService.addPlannedMovie(user, movie);

        assertEquals(MovieStatus.WANT_TO_WATCH, existingUserMovie.getStatus());
        verify(userMovieRepository, times(1)).save(existingUserMovie);
    }

    @Test
    void testAddSuggestedMovie_NotPresent() {
        AppUser friend = new AppUser();
        friend.setUsername("friendUser");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(userMovieRepository.findByUserAndMovieAndStatus(friend, movie, MovieStatus.WANT_TO_WATCH_BY_FRIEND))
                .thenReturn(Optional.empty());

        userMovieService.addSuggestedMovie(friend, movie, "suggestedByUser");

        verify(userMovieRepository, times(1)).save(any(UserMovie.class));
    }

    @Test
    void testUpdateFriendsMovieStatusToUnwatched() {
        AppUser user = new AppUser();
        user.setUsername("user");
        Movie movie = new Movie();
        List<AppUser> friends = List.of(new AppUser(), new AppUser());
        friends.get(0).setUsername("friend1");
        friends.get(1).setUsername("friend2");

        when(friendshipService.getFriends(user.getUsername())).thenReturn(friends);
        UserMovie friendMovie = new UserMovie();
        friendMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);

        when(userMovieRepository.findAllByMovieAndUserInAndStatus(movie, friends, MovieStatus.WANT_TO_WATCH_BY_FRIEND))
                .thenReturn(List.of(friendMovie));

        userMovieService.updateFriendsMovieStatusToUnwatched(user, movie);

        assertEquals(MovieStatus.UNWATCHED, friendMovie.getStatus());
        verify(userMovieRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testUpdateSuggestedMoviesStatus_ShouldClearSuggestedByForUnwatched() {
        AppUser user = new AppUser();
        AppUser friend = new AppUser();
        user.setUsername("user1");
        friend.setUsername("friend1");

        UserMovie suggestedMovie = new UserMovie();
        suggestedMovie.setUser(user);
        suggestedMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        suggestedMovie.setSuggestedBy(friend.getUsername());

        when(userMovieRepository.findByUserAndStatusAndSuggestedBy(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND, friend.getUsername()))
                .thenReturn(List.of(suggestedMovie));

        userMovieService.updateSuggestedMoviesStatus(user, friend, MovieStatus.UNWATCHED);

        assertNull(suggestedMovie.getSuggestedBy(), "suggestedBy should be cleared when setting status to UNWATCHED");
        assertEquals(MovieStatus.UNWATCHED, suggestedMovie.getStatus());
        verify(userMovieRepository, times(1)).save(suggestedMovie);
    }

    @Test
    void testUpdateSuggestedMoviesStatus_ShouldNotClearSuggestedByForOtherStatuses() {
        AppUser user = new AppUser();
        AppUser friend = new AppUser();
        user.setUsername("user1");
        friend.setUsername("friend1");

        UserMovie suggestedMovie = new UserMovie();
        suggestedMovie.setUser(user);
        suggestedMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        suggestedMovie.setSuggestedBy(friend.getUsername());

        when(userMovieRepository.findByUserAndStatusAndSuggestedBy(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND, friend.getUsername()))
                .thenReturn(List.of(suggestedMovie));

        userMovieService.updateSuggestedMoviesStatus(user, friend, MovieStatus.WANT_TO_WATCH);

        assertNotNull(suggestedMovie.getSuggestedBy(), "suggestedBy should not be cleared for statuses other than UNWATCHED");
        assertEquals(MovieStatus.WANT_TO_WATCH, suggestedMovie.getStatus());
        verify(userMovieRepository, times(1)).save(suggestedMovie);
    }


}
