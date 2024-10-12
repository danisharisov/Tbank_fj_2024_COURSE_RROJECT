package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.AppUserRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.UserMovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.RowSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserMovieService {

    @Autowired
    private AppUserService appUserService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserMovieRepository userMovieRepository;
    @Autowired
    private FriendshipService friendshipService;


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
        List<AppUser> friends = friendshipService.getFriends(user.getUsername());
        List<UserMovie> friendRatings = userMovieRepository.findByMovieAndUserIn(movie, friends);

        return friendRatings.stream()
                .mapToDouble(UserMovie::getRating)
                .average()
                .orElse(0.0);
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

}
