package com.example.Tbank_fj_2024_COURSE_PROJECT.models.user;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true)
    private String username;

    @Column(unique = true, nullable = false) // Telegram ID обязателен и уникален
    private String telegramId;
}
