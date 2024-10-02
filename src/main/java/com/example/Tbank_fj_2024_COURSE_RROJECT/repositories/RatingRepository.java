package com.example.Tbank_fj_2024_COURSE_RROJECT.repositories;

import com.example.Tbank_fj_2024_COURSE_RROJECT.models.movie.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}