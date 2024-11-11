package com.example.Tbank_fj_2024_COURSE_PROJECT.repositories;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.Friendship;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUserAndFriend(AppUser user, AppUser friend);

    List<Friendship> findAllByUser(AppUser user);

    List<Friendship> findAllByFriendAndStatus(AppUser friend, FriendshipStatus status);

    List<Friendship> findAllByUserAndStatus(AppUser user, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE f.user = :user AND f.friend = :friend AND f.status = 'PENDING'")
    Optional<Friendship> findOutgoingRequest(@Param("user") AppUser user, @Param("friend") AppUser friend);

    @Query("SELECT f FROM Friendship f WHERE (f.user = :user OR f.friend = :user) AND f.status = :status")
    List<Friendship> findAllByUserOrFriendAndStatus(@Param("user") AppUser user, @Param("status") FriendshipStatus status);

    @Query("SELECT f.friend FROM Friendship f WHERE f.user = :user AND f.status = 'ACCEPTED' " +
            "UNION SELECT f.user FROM Friendship f WHERE f.friend = :user AND f.status = 'ACCEPTED'")
    List<AppUser> findAcceptedFriends(@Param("user") AppUser user);
}