package com.mopl.moplwebsocketsse.domain.watch.dto;

import java.util.List;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;

public record CursorResponseWatchingSessionDto(
	List<WatchingSessionDto> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	long totalCount,
	WatchingSessionSortBy sortBy,
	SortDirection sortDirection
) {
}
