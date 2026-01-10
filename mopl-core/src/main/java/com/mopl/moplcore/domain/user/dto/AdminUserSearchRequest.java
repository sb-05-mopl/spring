package com.mopl.moplcore.domain.user.dto;

import java.util.UUID;

import com.mopl.moplcore.domain.user.entity.Role;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdminUserSearchRequest(
	String emailLike,
	Role roleEqual,
	Boolean isLocked,

	String cursor,
	UUID idAfter,

	@Min(1)
	@NotNull
	int limit,
	SortDirection sortDirection,
	SortBy sortBy
) {

	public AdminUserSearchRequest {
		if (sortBy == null) {
			sortBy = SortBy.createdAt;
		}
		if (sortDirection == null) {
			sortDirection = SortDirection.DESCENDING;
		}
	}
}
