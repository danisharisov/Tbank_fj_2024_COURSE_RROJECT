package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbMovieResponse;
import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbSearchResult;
import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OmdbServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OmdbService omdbService;

    @BeforeEach
    void setUp() throws Exception {
        omdbService = new OmdbService(restTemplate);

        Field apiKeyField = OmdbService.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(omdbService, "test_api_key");

        Field apiUrlField = OmdbService.class.getDeclaredField("apiUrl");
        apiUrlField.setAccessible(true);
        apiUrlField.set(omdbService, "http://www.omdbapi.com");
    }

    @Test
    void testGetMovieByImdbId_Found() {
        OmdbMovieResponse omdbMovieResponse = new OmdbMovieResponse();
        omdbMovieResponse.setTitle("Test Movie");
        omdbMovieResponse.setImdbID("tt1234567");
        omdbMovieResponse.setYear("2020");
        omdbMovieResponse.setImdbRating("8.0");
        omdbMovieResponse.setPoster("test_poster_url");
        omdbMovieResponse.setType("movie");
        omdbMovieResponse.setDirector("Test Director");
        omdbMovieResponse.setResponse("True");

        String url = "http://www.omdbapi.com/?i=tt1234567&apikey=test_api_key";
        when(restTemplate.getForObject(url, OmdbMovieResponse.class)).thenReturn(omdbMovieResponse);

        Movie movie = omdbService.getMovieByImdbId("tt1234567");

        assertNotNull(movie);
        assertEquals("Test Movie", movie.getTitle());
        assertEquals("tt1234567", movie.getImdbId());
        assertEquals("8.0", movie.getImdbRating());
        verify(restTemplate, times(1)).getForObject(url, OmdbMovieResponse.class);
    }

    @Test
    void testGetMovieByImdbId_NotFound() {
        String url = "http://www.omdbapi.com/?i=tt1234567&apikey=test_api_key";
        when(restTemplate.getForObject(url, OmdbMovieResponse.class)).thenReturn(null);

        Movie movie = omdbService.getMovieByImdbId("tt1234567");

        assertNull(movie);
        verify(restTemplate, times(1)).getForObject(url, OmdbMovieResponse.class);
    }

    @Test
    void testSearchMoviesByTitle_Found() {
        OmdbSearchResult searchResult = new OmdbSearchResult();
        OmdbMovie omdbMovie = new OmdbMovie();
        omdbMovie.setTitle("Test Movie");
        omdbMovie.setImdbId("tt1234567");
        omdbMovie.setYear("2020");
        omdbMovie.setType("movie");
        omdbMovie.setPoster("test_poster_url");

        searchResult.setResponse("True");
        searchResult.setSearch(Collections.singletonList(omdbMovie));
        searchResult.setTotalResults("1");

        String url = "http://www.omdbapi.com/?s=Test+Movie&type=movie&apikey=test_api_key";
        when(restTemplate.getForObject(url, OmdbSearchResult.class)).thenReturn(searchResult);

        List<Movie> movies = omdbService.searchMoviesByTitle("Test Movie");

        assertEquals(1, movies.size());
        assertEquals("Test Movie", movies.get(0).getTitle());
        assertEquals("tt1234567", movies.get(0).getImdbId());
        verify(restTemplate, times(1)).getForObject(url, OmdbSearchResult.class);
    }

    @Test
    void testSearchMoviesByTitle_NotFound() {
        OmdbSearchResult searchResult = new OmdbSearchResult();
        searchResult.setResponse("False");

        String url = "http://www.omdbapi.com/?s=Nonexistent+Movie&type=movie&apikey=test_api_key";
        when(restTemplate.getForObject(url, OmdbSearchResult.class)).thenReturn(searchResult);

        List<Movie> movies = omdbService.searchMoviesByTitle("Nonexistent Movie");

        assertTrue(movies.isEmpty());
        verify(restTemplate, times(1)).getForObject(url, OmdbSearchResult.class);
    }

}
