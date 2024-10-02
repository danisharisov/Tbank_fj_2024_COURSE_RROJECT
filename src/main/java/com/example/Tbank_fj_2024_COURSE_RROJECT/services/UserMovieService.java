package com.example.Tbank_fj_2024_COURSE_RROJECT.services;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.UserMovie;
import com.example.Tbank_fj_2024_COURSE_RROJECT.repositories.MovieRepository;
import com.example.Tbank_fj_2024_COURSE_RROJECT.repositories.UserMovieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserMovieService {

    @Autowired
    private UserMovieRepository userMovieRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Transactional
    public UserMovie addMovieToUser(AppUser user, Movie movie) {
        Movie existingMovie = movieRepository.findByTitle(movie.getTitle());
        if (existingMovie == null) {
            existingMovie = movieRepository.save(movie);
        }

        // Создаем объект UserMovie
        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(existingMovie);

        return userMovieRepository.save(userMovie);
    }
}
