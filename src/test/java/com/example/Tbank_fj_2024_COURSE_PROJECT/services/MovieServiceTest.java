package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.repositories.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private OmdbService omdbService;

    @InjectMocks
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тестируем успешный поиск фильма
    @Test
    void findMovieByImdbId_Found() {
        String imdbId = "tt1234567";
        Movie movie = new Movie();
        movie.setImdbId(imdbId);

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.of(movie));

        Optional<Movie> result = movieService.findMovieByImdbId(imdbId);

        assertTrue(result.isPresent());
        assertEquals(imdbId, result.get().getImdbId());
        verify(movieRepository, times(1)).findByImdbId(imdbId);
    }

    // Тестируем случай, когда фильм не найден
    @Test
    void findMovieByImdbId_NotFound() {
        String imdbId = "tt1234567";

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.empty());

        Optional<Movie> result = movieService.findMovieByImdbId(imdbId);

        assertFalse(result.isPresent());
        verify(movieRepository, times(1)).findByImdbId(imdbId);
    }

    // Тестируем успешное сохранение нового фильма
    @Test
    void saveMovie_NewMovie() {
        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(movieRepository.findByImdbId(movie.getImdbId())).thenReturn(Optional.empty());
        when(movieRepository.save(movie)).thenReturn(movie);

        Movie result = movieService.saveMovie(movie);

        assertEquals(movie, result);
        verify(movieRepository, times(1)).save(movie);
    }

    // Тестируем случай, когда фильм уже существует
    @Test
    void saveMovie_ExistingMovie() {
        Movie movie = new Movie();
        movie.setImdbId("tt1234567");

        when(movieRepository.findByImdbId(movie.getImdbId())).thenReturn(Optional.of(movie));

        Movie result = movieService.saveMovie(movie);

        assertEquals(movie, result);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    // Тестируем успешное сохранение или возврат фильма
    @Test
    void findOrSaveMovieByImdbId_NewMovie() {
        String imdbId = "tt1234567";
        Movie movie = new Movie();
        movie.setImdbId(imdbId);

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.empty());
        when(movieRepository.save(movie)).thenReturn(movie);

        Movie result = movieService.findOrSaveMovieByImdbId(imdbId, movie);

        assertEquals(movie, result);
        verify(movieRepository, times(1)).save(movie);
    }

    // Тестируем возврат существующего фильма
    @Test
    void findOrSaveMovieByImdbId_ExistingMovie() {
        String imdbId = "tt1234567";
        Movie movie = new Movie();
        movie.setImdbId(imdbId);

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.of(movie));

        Movie result = movieService.findOrSaveMovieByImdbId(imdbId, movie);

        assertEquals(movie, result);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    // Тестируем успешный возврат фильма
    @Test
    void getMovieByImdbId_Found() {
        String imdbId = "tt1234567";
        Movie movie = new Movie();
        movie.setImdbId(imdbId);

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.of(movie));

        Movie result = movieService.getMovieByImdbId(imdbId);

        assertEquals(movie, result);
        verify(movieRepository, times(1)).findByImdbId(imdbId);
    }

    // Тестируем выброс исключения при отсутствии фильма
    @Test
    void getMovieByImdbId_NotFound() {
        String imdbId = "tt1234567";

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> movieService.getMovieByImdbId(imdbId));

        assertEquals("Фильм с таким IMDb ID не найден: " + imdbId, exception.getMessage());
        verify(movieRepository, times(1)).findByImdbId(imdbId);
    }

    // Тестируем получение фильма из базы
    @Test
    void fetchAndSaveMovie_FoundInDatabase() {
        String imdbId = "tt1234567";
        Movie movie = new Movie();
        movie.setImdbId(imdbId);

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.of(movie));

        Movie result = movieService.fetchAndSaveMovie(imdbId);

        assertEquals(movie, result);
        verify(movieRepository, times(1)).findByImdbId(imdbId);
        verify(movieRepository, never()).save(any(Movie.class));
        verify(omdbService, never()).getMovieByImdbId(anyString());
    }

    // Тестируем получение фильма из OMDb и сохранение
    @Test
    void fetchAndSaveMovie_FetchedFromOmdb() {
        String imdbId = "tt1234567";
        Movie movie = new Movie();
        movie.setImdbId(imdbId);

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.empty());
        when(omdbService.getMovieByImdbId(imdbId)).thenReturn(movie);

        Movie result = movieService.fetchAndSaveMovie(imdbId);

        assertEquals(movie, result);
        verify(movieRepository, times(1)).save(movie);
        verify(omdbService, times(1)).getMovieByImdbId(imdbId);
    }

    // Тестируем случай, когда OMDb возвращает null
    @Test
    void fetchAndSaveMovie_OmdbReturnsNull() {
        String imdbId = "tt1234567";

        when(movieRepository.findByImdbId(imdbId)).thenReturn(Optional.empty());
        when(omdbService.getMovieByImdbId(imdbId)).thenReturn(null);

        Movie result = movieService.fetchAndSaveMovie(imdbId);

        assertNull(result);
        verify(movieRepository, never()).save(any(Movie.class));
        verify(omdbService, times(1)).getMovieByImdbId(imdbId);
    }
}
