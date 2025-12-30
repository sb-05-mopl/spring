package com.mopl.moplcore.domain.review.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
}
