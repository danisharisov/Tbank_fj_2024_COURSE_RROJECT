package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final OmdbService omdbService;

    @Autowired
    public MovieService(MovieRepository movieRepository, OmdbService omdbService) {
        this.movieRepository = movieRepository;
        this.omdbService = omdbService;
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

    public Movie fetchAndSaveMovie(String imdbId) {
        // Проверяем, есть ли фильм в базе данных
        Optional<Movie> optionalMovie = movieRepository.findByImdbId(imdbId);
        if (optionalMovie.isPresent()) {
            return optionalMovie.get();
        }

        // Если фильма нет в базе данных, получаем его из внешнего API (OMDb API)
        Movie movie = omdbService.getMovieByImdbId(imdbId);
        if (movie != null) {
            movieRepository.save(movie);
        }
        return movie;
    }

}
