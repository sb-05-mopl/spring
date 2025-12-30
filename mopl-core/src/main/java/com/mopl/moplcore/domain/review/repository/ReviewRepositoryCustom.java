package com.mopl.moplcore.domain.review.repository;

import java.util.List;
import java.util.UUID;

import com.mopl.moplcore.domain.review.entity.Review;

public interface ReviewRepositoryCustom {
	List<Review> findByContentIdWithCursor(
		UUID contentId,
		String cursor,
		UUID idAfter,
		int limit,
		String sortBy,
		String sortDirection
	);
}
