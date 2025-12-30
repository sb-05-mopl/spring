package com.mopl.moplcore.domain.review.dto;

import java.util.List;

public record CursorResponseReviewDto(
	List<ReviewDto> data,
	String nextCursor,
	String nextIdAfter,
	boolean hasNext,
	long totalCount,
	ReviewSortBy sortBy,
	ReviewSortDirection sortDirection
) {
}