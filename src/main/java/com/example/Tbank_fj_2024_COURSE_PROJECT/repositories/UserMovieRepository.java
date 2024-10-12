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
    List<UserMovie> findByUser(AppUser user);
    Optional<UserMovie> findByUserAndMovie(AppUser user, Movie movie);
    Optional<UserMovie> findByUserIdAndMovieImdbId(Long userId, String imdbId);
    Optional<UserMovie> findByUserAndMovieAndStatus(AppUser user, Movie movie, MovieStatus status);

    List<UserMovie> findByUserAndStatus(AppUser user, MovieStatus watched);

    List<UserMovie> findByMovieAndUserIn(Movie movie, List<AppUser> friends);

    @Query("SELECT COUNT(um) FROM UserMovie um WHERE um.movie.id = :movieId")
    long countByMovieId(@Param("movieId") Long movieId);
}