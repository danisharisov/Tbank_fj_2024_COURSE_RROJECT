package com.example.Tbank_fj_2024_COURSE_RROJECT.repositories;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.MovieStatus;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserMovieRepository extends JpaRepository<UserMovie, Long> {
    List<UserMovie> findByUserIdAndStatus(Long userId, MovieStatus status);
    List<UserMovie> findByUser(AppUser user);
}