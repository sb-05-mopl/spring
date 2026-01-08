package com.mopl.moplwebsocketsse.domain.watch.dto;

import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WatchingSessionSearchRequest(
	String watcherNameLike,

	String cursor,

	UUID idAfter,

	@Min(value = 1, message = "limit은 최소 1, 최대 100이어야 합니다.")
	@Max(value = 100, message = "limit은 최소 1, 최대 100이어야 합니다.")
	@NotNull(message = "limit은 필수입니다.")
	Integer limit,

	@NotNull(message = "정렬 방향은 필수입니다.")
	SortDirection sortDirection,

	@NotNull(message = "정렬 기준은 필수입니다.")
	WatchingSessionSortBy sortBy
) {
}