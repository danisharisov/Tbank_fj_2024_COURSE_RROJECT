package com.example.Tbank_fj_2024_COURSE_PROJECT.controllers;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.OmdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/omdb")
public class OmdbController {

    @Autowired
    private OmdbService omdbService;

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(@RequestParam String title) {
        List<Movie> movies = omdbService.searchMoviesByTitle(title);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{imdbId}")
    public ResponseEntity<Movie> getMovie(@PathVariable String imdbId) {
        Movie movie = omdbService.getMovieByImdbId(imdbId);
        return ResponseEntity.ok(movie);
    }
}