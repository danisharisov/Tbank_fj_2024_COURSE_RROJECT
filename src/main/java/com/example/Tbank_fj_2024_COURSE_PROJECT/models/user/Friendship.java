package com.example.Tbank_fj_2024_COURSE_PROJECT.models.user;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friendship")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private AppUser friend;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;
}

