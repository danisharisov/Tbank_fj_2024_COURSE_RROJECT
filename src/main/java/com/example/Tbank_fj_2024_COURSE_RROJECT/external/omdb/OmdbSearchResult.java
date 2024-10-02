package com.example.Tbank_fj_2024_COURSE_RROJECT.external.omdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class OmdbSearchResult {

    @JsonProperty("Search")
    private List<OmdbMovie> search;

    @JsonProperty("totalResults")
    private String totalResults;

    @JsonProperty("Response")
    private String response;


}
