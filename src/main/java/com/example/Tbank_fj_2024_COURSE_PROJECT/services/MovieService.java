package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }
    public Optional<Movie> findMovieByImdbId(String imdbId) {
        return movieRepository.findByImdbId(imdbId);
    }
    public Movie saveMovie(Movie movie) {
        return movieRepository.findByImdbId(movie.getImdbId())
                .orElseGet(() -> movieRepository.save(movie));
    }

    public Movie findOrSaveMovieByImdbId(String imdbId, Movie movie) {
        return movieRepository.findByImdbId(imdbId).orElseGet(() -> movieRepository.save(movie));
    }

    public Movie getMovieByImdbId(String imdbId) {
        return movieRepository.findByImdbId(imdbId)
                .orElseThrow(() -> new IllegalArgumentException("Фильм с таким IMDb ID не найден: " + imdbId));
    }

}
