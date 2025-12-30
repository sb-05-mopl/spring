package com.mopl.moplcore.domain.review.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mopl.moplcore.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {
	@Query("SELECT COUNT(r) FROM Review r WHERE r.content.id = :contentId")
	long count(UUID contentId);

	boolean existsByAuthorIdAndContentId(UUID authorId, UUID contentId);
}
