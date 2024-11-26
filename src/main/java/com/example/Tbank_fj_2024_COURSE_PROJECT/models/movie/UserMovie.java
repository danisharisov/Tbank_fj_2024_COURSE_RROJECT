package com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_movie")
@EqualsAndHashCode(exclude = "user")
public class UserMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    @Column(name = "user_rating", nullable = true)
    private Double Rating;

    @Min(0)
    @Max(100)
    @Column(nullable = true)
    private Integer hype;

    @Column(name = "suggested_by")
    private String suggestedBy;

    public UserMovie(AppUser user, Movie movie) {
        this.user = user;
        this.movie = movie;
    }
}