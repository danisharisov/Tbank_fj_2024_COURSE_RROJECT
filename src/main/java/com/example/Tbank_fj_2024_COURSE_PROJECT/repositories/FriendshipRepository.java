package com.example.Tbank_fj_2024_COURSE_PROJECT.repositories;

import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.Friendship;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.AppUser;
import com.example.Tbank_fj_2024_COURSE_PROJECT.models.user.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUserAndFriend(AppUser user, AppUser friend);

    List<Friendship> findAllByUser(AppUser user);

    List<Friendship> findAllByFriendAndStatus(AppUser friend, FriendshipStatus status);

    List<Friendship> findAllByUserAndStatus(AppUser user, FriendshipStatus status);
}