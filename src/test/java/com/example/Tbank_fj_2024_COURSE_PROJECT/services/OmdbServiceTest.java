package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbMovieResponse;
import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbSearchResult;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OmdbServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OmdbService omdbService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Тестируем успешное получение фильма по IMDb ID
    @Test
    void getMovieByImdbId_Success() {
        String imdbId = "tt1234567";
        OmdbMovieResponse response = new OmdbMovieResponse();
        response.setResponse("True");
        response.setTitle("Test Movie");
        response.setImdbID(imdbId);

        when(restTemplate.getForObject(anyString(), eq(OmdbMovieResponse.class))).thenReturn(response);

        Movie result = omdbService.getMovieByImdbId(imdbId);

        assertNotNull(result);
        assertEquals("Test Movie", result.getTitle());
        assertEquals(imdbId, result.getImdbId());
    }

    // Тестируем случай, когда OMDb возвращает пустой или некорректный ответ
    @Test
    void getMovieByImdbId_EmptyResponse() {
        String imdbId = "tt1234567";
        OmdbMovieResponse response = new OmdbMovieResponse();
        response.setResponse("False");

        when(restTemplate.getForObject(anyString(), eq(OmdbMovieResponse.class))).thenReturn(response);

        Movie result = omdbService.getMovieByImdbId(imdbId);

        assertNull(result);
    }

    // Тестируем ошибку при запросе к OMDb API
    @Test
    void getMovieByImdbId_ApiError() {
        String imdbId = "tt1234567";

        when(restTemplate.getForObject(anyString(), eq(OmdbMovieResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        Movie result = omdbService.getMovieByImdbId(imdbId);

        assertNull(result);
    }

    // Тестируем успешный поиск фильмов по названию
    @Test
    void searchMoviesByTitle_Success() {
        String title = "Test Movie";
        OmdbSearchResult searchResult = new OmdbSearchResult();
        searchResult.setResponse("True");
        OmdbMovie movie = new OmdbMovie();
        movie.setTitle("Test Movie");
        movie.setImdbId("tt1234567");
        searchResult.setSearch(List.of(movie));

        when(restTemplate.getForObject(anyString(), eq(OmdbSearchResult.class))).thenReturn(searchResult);

        List<Movie> movies = omdbService.searchMoviesByTitle(title);

        assertNotNull(movies);
        assertEquals(1, movies.size());
        assertEquals("Test Movie", movies.get(0).getTitle());
    }

    // Тестируем пустой ответ от OMDb API при поиске
    @Test
    void searchMoviesByTitle_EmptyResponse() {
        String title = "Test Movie";
        OmdbSearchResult searchResult = new OmdbSearchResult();
        searchResult.setResponse("False");

        when(restTemplate.getForObject(anyString(), eq(OmdbSearchResult.class))).thenReturn(searchResult);

        List<Movie> movies = omdbService.searchMoviesByTitle(title);

        assertTrue(movies.isEmpty());
    }

    // Тестируем ошибку при выполнении запроса на поиск
    @Test
    void searchMoviesByTitle_ApiError() {
        String title = "Test Movie";

        when(restTemplate.getForObject(anyString(), eq(OmdbSearchResult.class)))
                .thenThrow(new RuntimeException("API Error"));

        List<Movie> movies = omdbService.searchMoviesByTitle(title);

        assertTrue(movies.isEmpty());
    }

    // Тестируем преобразование ответа OMDb в объект Movie
    @Test
    void mapOmdbResponseToMovie_Success() {
        OmdbMovieResponse response = new OmdbMovieResponse();
        response.setTitle("Test Movie");
        response.setImdbID("tt1234567");

        Movie movie = omdbService.mapOmdbResponseToMovie(response);

        assertNotNull(movie);
        assertEquals("Test Movie", movie.getTitle());
        assertEquals("tt1234567", movie.getImdbId());
    }

    // Тестируем преобразование результата поиска OMDb в список Movie
    @Test
    void mapOmdbSearchResultToMovies_Success() {
        OmdbMovie omdbMovie = new OmdbMovie();
        omdbMovie.setTitle("Test Movie");
        omdbMovie.setImdbId("tt1234567");

        OmdbSearchResult searchResult = new OmdbSearchResult();
        searchResult.setSearch(List.of(omdbMovie));

        List<Movie> movies = omdbService.mapOmdbSearchResultToMovies(searchResult);

        assertNotNull(movies);
        assertEquals(1, movies.size());
        assertEquals("Test Movie", movies.get(0).getTitle());
    }

    // Тестируем успешное кодирование строки
    @Test
    void encodeValue_Success() {
        String value = "Test Movie";
        String encodedValue = omdbService.encodeValue(value);

        assertEquals("Test+Movie", encodedValue);
    }
}
