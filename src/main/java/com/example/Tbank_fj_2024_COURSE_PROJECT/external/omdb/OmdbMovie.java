package com.example.Tbank_fj_2024_COURSE_PROJECT.external.omdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OmdbMovie {
    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("imdbID")
    private String imdbId;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Poster")
    private String poster;

}
