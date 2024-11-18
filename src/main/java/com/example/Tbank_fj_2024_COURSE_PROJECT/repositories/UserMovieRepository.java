package com.example.Tbank_fj_2024_COURSE_PROJECT.repositories;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserMovieRepository extends JpaRepository<UserMovie, Long> {
    List<UserMovie> findByUserIdAndStatus(Long userId, MovieStatus status);
    Optional<UserMovie> findByUserAndMovie(AppUser user, Movie movie);
    Optional<UserMovie> findByUserAndMovieAndStatus(AppUser user, Movie movie, MovieStatus status);

    List<UserMovie> findByUserAndStatus(AppUser user, MovieStatus status);

    List<UserMovie> findByMovieAndUserIn(Movie movie, List<AppUser> friends);

    List<UserMovie> findAllByMovieAndUserInAndStatusIn(Movie movie, List<AppUser> friends, List<MovieStatus> statuses);


    List<UserMovie> findByUserAndStatusAndSuggestedBy(AppUser user, MovieStatus status, String suggestedBy);

    List<UserMovie> findAllByMovieAndUserInAndStatus(Movie movie, List<AppUser> users, MovieStatus status);
    List<UserMovie> findAllByUserAndMovieAndStatus(AppUser user, Movie movie, MovieStatus status);

}