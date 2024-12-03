package com.example.Tbank_fj_2024_COURSE_PROJECT.services;

import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbMovieResponse;
import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbSearchResult;
import com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb.OmdbMovie;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OmdbService {

    private static final Logger logger = LoggerFactory.getLogger(OmdbService.class);

    @Value("${omdb.api.url}")
    private String apiUrl;

    @Value("${omdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public OmdbService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Movie getMovieByImdbId(String imdbId) {
        String url = String.format("%s/?i=%s&apikey=%s", apiUrl, imdbId, apiKey);
        try {
            logger.info("Request URL for movie by IMDb ID: {}", url);
            OmdbMovieResponse omdbMovieResponse = restTemplate.getForObject(url, OmdbMovieResponse.class);
            if (omdbMovieResponse != null && "True".equalsIgnoreCase(omdbMovieResponse.getResponse())) {
                logger.info("Found movie: {}", omdbMovieResponse.getTitle());
                return mapOmdbResponseToMovie(omdbMovieResponse);
            } else {
                logger.warn("Movie not found or incorrect response for IMDb ID: {}", imdbId);
            }
        } catch (Exception e) {
            logger.error("Error fetching movie by IMDb ID: {}", imdbId, e);
        }
        return null;
    }

    public List<Movie> searchMoviesByTitle(String title) {
        String url = String.format("%s/?s=%s&type=movie&apikey=%s", apiUrl, encodeValue(title), apiKey);
        try {
            logger.info("Request URL for search by title: {}", url);
            OmdbSearchResult searchResult = restTemplate.getForObject(url, OmdbSearchResult.class);
            if (searchResult != null && "True".equalsIgnoreCase(searchResult.getResponse())) {
                logger.info("OMDb API returned {} results", searchResult.getTotalResults());
                return mapOmdbSearchResultToMovies(searchResult);
            } else {
                logger.warn("OMDb API response is empty or invalid for title: {}", title);
            }
        } catch (Exception e) {
            logger.error("Error searching movies by title: {}", title, e);
        }
        return Collections.emptyList();
    }

    protected Movie mapOmdbResponseToMovie(OmdbMovieResponse omdbMovieResponse) {
        Movie movie = new Movie();
        movie.setTitle(omdbMovieResponse.getTitle());
        movie.setYear(omdbMovieResponse.getYear());
        movie.setImdbId(omdbMovieResponse.getImdbID());
        movie.setImdbRating(omdbMovieResponse.getImdbRating());
        movie.setPoster(omdbMovieResponse.getPoster());
        movie.setType(omdbMovieResponse.getType());
        movie.setDirector(omdbMovieResponse.getDirector() != null ? omdbMovieResponse.getDirector() : "Unknown Director");
        return movie;
    }

    protected List<Movie> mapOmdbSearchResultToMovies(OmdbSearchResult searchResult) {
        List<Movie> movies = new ArrayList<>();
        for (OmdbMovie omdbMovie : searchResult.getSearch()) {
            Movie movie = new Movie();
            movie.setTitle(omdbMovie.getTitle());
            movie.setYear(omdbMovie.getYear());
            movie.setImdbId(omdbMovie.getImdbId());
            movie.setImdbRating(omdbMovie.getImdbRating());
            movie.setPoster(omdbMovie.getPoster());
            movie.setType(omdbMovie.getType());
            movie.setDirector("Unknown Director");
            movies.add(movie);
            logger.info("Added movie: {}", movie.getTitle());
        }
        return movies;
    }

    protected String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error encoding value", e);
        }
    }
}
