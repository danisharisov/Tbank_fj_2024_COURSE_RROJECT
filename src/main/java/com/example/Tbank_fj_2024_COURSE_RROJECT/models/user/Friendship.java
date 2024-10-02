package com.example.Tbank_fj_2024_COURSE_RROJECT.models.user;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import jakarta.persistence.*;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public AppUser getFriend() {
        return friend;
    }

    public void setFriend(AppUser friend) {
        this.friend = friend;
    }
}