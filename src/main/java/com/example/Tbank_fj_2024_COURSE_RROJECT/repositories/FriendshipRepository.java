package com.example.Tbank_fj_2024_COURSE_RROJECT.repositories;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.Friendship;
import com.example.Tbank_fj_2024_COURSE_RROJECT.models.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Найти всех друзей пользователя
    List<Friendship> findByUser(AppUser appUser);

    // Найти всех пользователей, у которых указанный пользователь в друзьях
    List<Friendship> findByFriend(AppUser friend);

    // Проверить, есть ли дружба между двумя пользователями
    boolean existsByUserAndFriend(AppUser appUser, AppUser friend);

    // Удалить дружбу между двумя пользователями
    void deleteByUserAndFriend(AppUser appUser, AppUser friend);
}