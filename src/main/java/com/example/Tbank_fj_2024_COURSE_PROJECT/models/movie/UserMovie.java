package com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;


@Data
@Entity
@Table(name = "user_movie")
@EqualsAndHashCode(exclude = "user")
public class UserMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status; // Статус фильма: WATCHED или WANT_TO_WATCH

    @Column(name = "user_rating", nullable = true)
    private Double Rating;

    @Column(nullable = true)
    private Integer hype; // Уровень ажиотажа (опционально)
}