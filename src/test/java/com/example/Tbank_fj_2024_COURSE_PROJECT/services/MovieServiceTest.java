package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.MovieService;
import com.example.Tbank_fj_2024_COURSE_PROJECT.services.OmdbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private OmdbService omdbService;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = new Movie();
        testMovie.setImdbId("tt1234567");
    }

    @Test
    void testFindMovieByImdbId_Found() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.of(testMovie));

        Optional<Movie> result = movieService.findMovieByImdbId(testMovie.getImdbId());

        assertTrue(result.isPresent());
        assertEquals(testMovie.getImdbId(), result.get().getImdbId());
    }

    @Test
    void testFindMovieByImdbId_NotFound() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.empty());

        Optional<Movie> result = movieService.findMovieByImdbId(testMovie.getImdbId());

        assertFalse(result.isPresent());
    }

    @Test
    void testSaveMovie_ExistingMovie() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.of(testMovie));

        Movie result = movieService.saveMovie(testMovie);

        assertEquals(testMovie, result);
        verify(movieRepository, never()).save(testMovie);
    }

    @Test
    void testSaveMovie_NewMovie() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.empty());
        when(movieRepository.save(testMovie)).thenReturn(testMovie);

        Movie result = movieService.saveMovie(testMovie);

        assertEquals(testMovie, result);
        verify(movieRepository, times(1)).save(testMovie);
    }

    @Test
    void testFindOrSaveMovieByImdbId_ExistingMovie() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.of(testMovie));

        Movie result = movieService.findOrSaveMovieByImdbId(testMovie.getImdbId(), testMovie);

        assertEquals(testMovie, result);
        verify(movieRepository, never()).save(testMovie);
    }

    @Test
    void testFindOrSaveMovieByImdbId_NewMovie() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.empty());
        when(movieRepository.save(testMovie)).thenReturn(testMovie);

        Movie result = movieService.findOrSaveMovieByImdbId(testMovie.getImdbId(), testMovie);

        assertEquals(testMovie, result);
        verify(movieRepository, times(1)).save(testMovie);
    }

    @Test
    void testGetMovieByImdbId_Found() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.of(testMovie));

        Movie result = movieService.getMovieByImdbId(testMovie.getImdbId());

        assertEquals(testMovie, result);
    }

    @Test
    void testGetMovieByImdbId_NotFound() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            movieService.getMovieByImdbId(testMovie.getImdbId());
        });

        assertTrue(exception.getMessage().contains("Фильм с таким IMDb ID не найден"));
    }

    @Test
    void testFetchAndSaveMovie_ExistingMovie() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.of(testMovie));

        Movie result = movieService.fetchAndSaveMovie(testMovie.getImdbId());

        assertEquals(testMovie, result);
        verify(movieRepository, never()).save(testMovie);
    }

    @Test
    void testFetchAndSaveMovie_NewMovie() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.empty());
        when(omdbService.getMovieByImdbId(testMovie.getImdbId())).thenReturn(testMovie);
        when(movieRepository.save(testMovie)).thenReturn(testMovie);

        Movie result = movieService.fetchAndSaveMovie(testMovie.getImdbId());

        assertEquals(testMovie, result);
        verify(movieRepository, times(1)).save(testMovie);
    }

    @Test
    void testFetchAndSaveMovie_NotFoundInOmdb() {
        when(movieRepository.findByImdbId(testMovie.getImdbId())).thenReturn(Optional.empty());
        when(omdbService.getMovieByImdbId(testMovie.getImdbId())).thenReturn(null);

        Movie result = movieService.fetchAndSaveMovie(testMovie.getImdbId());

        assertNull(result);
        verify(movieRepository, never()).save(any());
    }
}

