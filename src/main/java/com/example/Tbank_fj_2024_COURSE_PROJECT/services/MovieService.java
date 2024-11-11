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

    // Поиск фильма по его IMDb ID в базе данных
    public Optional<Movie> findMovieByImdbId(String imdbId) {
        return movieRepository.findByImdbId(imdbId);
    }

    // Сохранение нового фильма в базе данных
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

}
