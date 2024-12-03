package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.UserMovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.telegram.services.MessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тестируем успешное добавление предложенного фильма
    @Test
    void addSuggestedMovie_Success() {
        AppUser friend = new AppUser();
        friend.setUsername("friend");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie newUserMovie = new UserMovie();
        newUserMovie.setMovie(movie);
        newUserMovie.setUser(friend);
        newUserMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);

        when(userMovieRepository.findByUserAndMovie(friend, movie)).thenReturn(Optional.empty());
        when(userMovieRepository.save(any(UserMovie.class))).thenReturn(newUserMovie);

        userMovieService.addSuggestedMovie(friend, movie, "user");

        verify(userMovieRepository, times(2)).save(any(UserMovie.class));
    }

    // Тестируем, что предложенный фильм уже добавлен (не обновляется)
    @Test
    void addSuggestedMovie_AlreadySuggested_NoUpdate() {
        AppUser friend = new AppUser();
        friend.setUsername("friend");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie userMovie = new UserMovie();
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        userMovie.setSuggestedBy("user");

        when(userMovieRepository.findByUserAndMovie(friend, movie)).thenReturn(Optional.of(userMovie));

        userMovieService.addSuggestedMovie(friend, movie, "user");

        verify(userMovieRepository, never()).save(any(UserMovie.class));
    }

    // Тестируем успешное добавление фильма в запланированные
    @Test
    void addPlannedMovie_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(movieService.findOrSaveMovieByImdbId(movie.getImdbId(), movie)).thenReturn(movie);
        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.empty());

        userMovieService.addPlannedMovie(user, movie);

        verify(userMovieRepository, times(1)).save(any(UserMovie.class));
        verify(friendshipService, times(1)).getFriends(user.getUsername());
    }

    // Тестируем случай, когда фильм уже запланирован
    @Test
    void addPlannedMovie_AlreadyPlanned() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie userMovie = new UserMovie();
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        when(movieService.findOrSaveMovieByImdbId(movie.getImdbId(), movie)).thenReturn(movie);
        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.of(userMovie));

        userMovieService.addPlannedMovie(user, movie);

        verify(userMovieRepository, never()).save(any(UserMovie.class));
    }

    // Тестируем успешное добавление фильма в просмотренные
    @Test
    void addWatchedMovie_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.empty());

        userMovieService.addWatchedMovie(user, movie, "chatId");

        verify(userMovieRepository, times(1)).save(any(UserMovie.class));
        verify(messageSender, times(1)).sendMessage(eq("chatId"), anyString());
    }

    // Тестируем случай, когда фильм уже добавлен в просмотренные
    @Test
    void addWatchedMovie_AlreadyWatched() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie userMovie = new UserMovie();
        userMovie.setStatus(MovieStatus.WATCHED);

        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.of(userMovie));

        userMovieService.addWatchedMovie(user, movie, "chatId");

        verify(userMovieRepository, never()).save(any(UserMovie.class));
        verify(messageSender, times(1)).sendMessage(eq("chatId"), anyString());
    }

    // Тестируем успешное изменение статуса фильма на UNWATCHED
    @Test
    void setMovieStatusForUserToUnwatched_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie userMovie = new UserMovie();
        userMovie.setMovie(movie);
        userMovie.setUser(user);
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.of(userMovie));
        when(friendshipService.getFriends(user.getUsername())).thenReturn(List.of());

        userMovieService.setMovieStatusForUserToUnwatched(user, movie);

        verify(userMovieRepository, times(1)).save(userMovie);
        assertEquals(MovieStatus.UNWATCHED, userMovie.getStatus());
    }

    // Тестируем выброс исключения, если фильм не найден
    @Test
    void setMovieStatusForUserToUnwatched_MovieNotFound() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userMovieService.setMovieStatusForUserToUnwatched(user, movie));

        assertEquals("Фильм не найден у пользователя.", exception.getMessage());
        verify(userMovieRepository, never()).save(any(UserMovie.class));
    }

    // Тестируем успешное добавление оценки фильму
    @Test
    void addRating_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie userMovie = new UserMovie();
        userMovie.setMovie(movie);
        userMovie.setUser(user);
        userMovie.setStatus(MovieStatus.WATCHED);

        when(appUserService.findByUsername(user.getUsername())).thenReturn(user);
        when(movieService.getMovieByImdbId(movie.getImdbId())).thenReturn(movie);
        when(userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WATCHED))
                .thenReturn(Optional.of(userMovie));

        userMovieService.addRating(user.getUsername(), movie.getImdbId(), 8.5);

        assertEquals(8.5, userMovie.getRating());
        verify(userMovieRepository, times(1)).save(userMovie);
    }

    // Тестируем добавление оценки, если фильм не найден
    @Test
    void addRating_MovieNotFound() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(appUserService.findByUsername(user.getUsername())).thenReturn(user);
        when(movieService.getMovieByImdbId(movie.getImdbId())).thenReturn(movie);
        when(userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WATCHED))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userMovieService.addRating(user.getUsername(), movie.getImdbId(), 8.5));

        assertEquals("Фильм не найден в списке просмотренных.", exception.getMessage());
        verify(userMovieRepository, never()).save(any(UserMovie.class));
    }

    // Тестируем успешный расчёт средней оценки друзей для фильма
    @Test
    void getAverageFriendRating_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        AppUser friend1 = new AppUser();
        friend1.setUsername("friend1");

        AppUser friend2 = new AppUser();
        friend2.setUsername("friend2");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie friendMovie1 = new UserMovie();
        friendMovie1.setUser(friend1);
        friendMovie1.setMovie(movie);
        friendMovie1.setRating(7.5);

        UserMovie friendMovie2 = new UserMovie();
        friendMovie2.setUser(friend2);
        friendMovie2.setMovie(movie);
        friendMovie2.setRating(9.0);

        when(friendshipService.getFriends(user.getUsername())).thenReturn(List.of(friend1, friend2));
        when(userMovieRepository.findByMovieAndUserIn(eq(movie), anyList()))
                .thenReturn(List.of(friendMovie1, friendMovie2));

        double averageRating = userMovieService.getAverageFriendRating(user, movie);

        assertEquals(8.25, averageRating, 0.01);
    }

    // Тестируем расчёт средней оценки друзей, если оценок нет
    @Test
    void getAverageFriendRating_NoRatings() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(friendshipService.getFriends(user.getUsername())).thenReturn(List.of());
        when(userMovieRepository.findByMovieAndUserIn(eq(movie), anyList())).thenReturn(List.of());

        double averageRating = userMovieService.getAverageFriendRating(user, movie);

        assertEquals(0.0, averageRating, 0.01);
    }

    // Тестируем успешный расчёт среднего уровня ажиотажа друзей для фильма
    @Test
    void getAverageFriendHype_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        AppUser friend1 = new AppUser();
        friend1.setUsername("friend1");

        AppUser friend2 = new AppUser();
        friend2.setUsername("friend2");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie friendMovie1 = new UserMovie();
        friendMovie1.setUser(friend1);
        friendMovie1.setMovie(movie);
        friendMovie1.setHype(5);

        UserMovie friendMovie2 = new UserMovie();
        friendMovie2.setUser(friend2);
        friendMovie2.setMovie(movie);
        friendMovie2.setHype(10);

        when(friendshipService.getFriends(user.getUsername())).thenReturn(List.of(friend1, friend2));
        when(userMovieRepository.findAllByMovieAndUserInAndStatusIn(eq(movie), anyList(), anyList()))
                .thenReturn(List.of(friendMovie1, friendMovie2));

        double averageHype = userMovieService.getAverageFriendHype(user, movie);

        assertEquals(7.5, averageHype, 0.01);
    }

    // Тестируем расчёт среднего уровня ажиотажа друзей, если данные отсутствуют
    @Test
    void getAverageFriendHype_NoData() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(friendshipService.getFriends(user.getUsername())).thenReturn(List.of());
        when(userMovieRepository.findAllByMovieAndUserInAndStatusIn(eq(movie), anyList(), anyList()))
                .thenReturn(List.of());

        double averageHype = userMovieService.getAverageFriendHype(user, movie);

        assertEquals(0.0, averageHype, 0.01);
    }

    // Тестируем получение комбинированного списка запланированных фильмов
    @Test
    void getCombinedPlannedMovies_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie1 = new Movie();
        movie1.setImdbId("tt1234567");

        Movie movie2 = new Movie();
        movie2.setImdbId("tt7654321");

        UserMovie userPlannedMovie = new UserMovie();
        userPlannedMovie.setMovie(movie1);
        userPlannedMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        UserMovie friendSuggestedMovie = new UserMovie();
        friendSuggestedMovie.setMovie(movie2);
        friendSuggestedMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);

        when(userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH))
                .thenReturn(List.of(userPlannedMovie));
        when(userMovieRepository.findByUserAndStatus(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND))
                .thenReturn(List.of(friendSuggestedMovie));

        List<UserMovie> combinedMovies = userMovieService.getCombinedPlannedMovies(user);

        assertEquals(2, combinedMovies.size());
        assertTrue(combinedMovies.contains(userPlannedMovie));
        assertTrue(combinedMovies.contains(friendSuggestedMovie));
    }

    // Тестируем проверку, является ли пользователь владельцем фильма
    @Test
    void isMovieOwner_True() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie userMovie = new UserMovie();
        userMovie.setMovie(movie);
        userMovie.setUser(user);
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.of(userMovie));

        boolean isOwner = userMovieService.isMovieOwner(user, movie);

        assertTrue(isOwner);
    }

    // Тестируем проверку, является ли пользователь владельцем фильма (не владелец)
    @Test
    void isMovieOwner_False() {
        AppUser user = new AppUser();
        user.setUsername("user");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(userMovieRepository.findByUserAndMovie(user, movie)).thenReturn(Optional.empty());

        boolean isOwner = userMovieService.isMovieOwner(user, movie);

        assertFalse(isOwner);
    }

    // Тестируем обновление статуса предложенных фильмов
    @Test
    void updateSuggestedMoviesStatus_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        AppUser friend = new AppUser();
        friend.setUsername("friend");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie suggestedMovie = new UserMovie();
        suggestedMovie.setMovie(movie);
        suggestedMovie.setUser(user);
        suggestedMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        suggestedMovie.setSuggestedBy(friend.getUsername());

        when(userMovieRepository.findByUserAndStatusAndSuggestedBy(user, MovieStatus.WANT_TO_WATCH_BY_FRIEND, friend.getUsername()))
                .thenReturn(List.of(suggestedMovie));

        userMovieService.updateSuggestedMoviesStatus(user, friend, MovieStatus.UNWATCHED);

        assertEquals(MovieStatus.UNWATCHED, suggestedMovie.getStatus());
        assertNull(suggestedMovie.getSuggestedBy());
        verify(userMovieRepository, times(1)).save(suggestedMovie);
    }

    // Тестируем изменение статуса фильма у друзей на UNWATCHED
    @Test
    void updateFriendsMovieStatusToUnwatched_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        AppUser friend = new AppUser();
        friend.setUsername("friend");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie friendMovie = new UserMovie();
        friendMovie.setMovie(movie);
        friendMovie.setUser(friend);
        friendMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);

        when(friendshipService.getFriends(user.getUsername())).thenReturn(List.of(friend));
        when(userMovieRepository.findAllByMovieAndUserInAndStatus(movie, List.of(friend), MovieStatus.WANT_TO_WATCH_BY_FRIEND))
                .thenReturn(List.of(friendMovie));

        userMovieService.updateFriendsMovieStatusToUnwatched(user, movie);

        assertEquals(MovieStatus.UNWATCHED, friendMovie.getStatus());
        verify(userMovieRepository, times(1)).saveAll(List.of(friendMovie));
    }

    // Тестируем удаление запланированного фильма с обновлением у друзей
    @Test
    void removePlannedMovieAndUpdateFriends_Success() {
        AppUser user = new AppUser();
        user.setUsername("user");

        AppUser friend = new AppUser();
        friend.setUsername("friend");

        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        UserMovie userMovie = new UserMovie();
        userMovie.setMovie(movie);
        userMovie.setUser(user);
        userMovie.setStatus(MovieStatus.WANT_TO_WATCH);

        UserMovie friendMovie = new UserMovie();
        friendMovie.setMovie(movie);
        friendMovie.setUser(friend);
        friendMovie.setStatus(MovieStatus.WANT_TO_WATCH_BY_FRIEND);
        friendMovie.setSuggestedBy(user.getUsername());

        when(userMovieRepository.findByUserAndMovieAndStatus(user, movie, MovieStatus.WANT_TO_WATCH))
                .thenReturn(Optional.of(userMovie));
        when(friendshipService.getFriends(user.getUsername())).thenReturn(List.of(friend));
        when(userMovieRepository.findByUserAndMovie(friend, movie)).thenReturn(Optional.of(friendMovie));

        userMovieService.removePlannedMovieAndUpdateFriends(user, movie);

        assertEquals(MovieStatus.UNWATCHED, userMovie.getStatus());
        assertEquals(MovieStatus.UNWATCHED, friendMovie.getStatus());
        assertNull(friendMovie.getSuggestedBy());

        verify(userMovieRepository, times(2)).save(userMovie);
        verify(userMovieRepository, times(2)).save(friendMovie);
    }



}
