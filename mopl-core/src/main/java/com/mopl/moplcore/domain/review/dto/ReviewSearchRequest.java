package com.mopl.moplcore.domain.review.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewSearchRequest(
	UUID contentId,

	String cursor,

	UUID idAfter,

	@Min(value = 1, message = "limit은 최소 1, 최대 100이어야 합니다.")
	@Max(value = 100, message = "limit은 최소 1, 최대 100이어야 합니다.")
	@NotNull(message = "limit은 최소 1, 최대 100이어야 합니다.")
	int limit,

	@NotNull(message = "정렬 방향은 필수입니다.")
	ReviewSortDirection sortDirection,

	@NotNull(message = "정렬 기준은 필수입니다.")
	ReviewSortBy sortBy
) {
}
