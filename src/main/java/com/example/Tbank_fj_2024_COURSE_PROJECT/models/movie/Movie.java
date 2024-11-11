package com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movie", uniqueConstraints = @UniqueConstraint(columnNames = "imdbId"))
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String year;

    @Column(nullable = false, unique = true)
    private String imdbId;

    private String poster;

    @Column(nullable = true)
    private String type;  // Тип контента (например, "movie", "series")

    @Column(nullable = true)
    private String director;

    @Column(nullable = true)
    private String imdbRating;

}
