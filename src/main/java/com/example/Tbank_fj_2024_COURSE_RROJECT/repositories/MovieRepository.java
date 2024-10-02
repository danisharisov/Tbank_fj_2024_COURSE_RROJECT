package com.example.Tbank_fj_2024_COURSE_RROJECT.repositories;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Movie findByTitle(String title);
    Optional<Movie> findByImdbId(String imdbId);
}