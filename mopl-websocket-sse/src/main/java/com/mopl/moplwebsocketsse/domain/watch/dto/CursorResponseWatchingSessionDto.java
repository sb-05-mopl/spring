package com.mopl.moplwebsocketsse.domain.watch.dto;

import java.util.List;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;

public record CursorResponseWatchingSessionDto(
	List<WatchingSessionDto> data,
	String nextCursor,
	String nextIdAfter,
	boolean hasNext,
	long totalCount,
	String sortBy,
	SortDirection sortDirection
) {
}
