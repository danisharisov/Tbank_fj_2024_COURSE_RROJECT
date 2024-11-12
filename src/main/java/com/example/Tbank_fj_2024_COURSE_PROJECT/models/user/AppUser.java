package com.example.Tbank_fj_2024_COURSE_PROJECT.models.user;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.movie.UserMovie;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user")
@EqualsAndHashCode(exclude = "movies")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String telegramId; // Telegram ID пользователя, если требуется

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "friendship",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<AppUser> friends = new HashSet<>(); // Список друзей

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserMovie> watchedMovies = new HashSet<>(); // Связь с фильмами


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserMovie> plannedMovies = new HashSet<>();

    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

}