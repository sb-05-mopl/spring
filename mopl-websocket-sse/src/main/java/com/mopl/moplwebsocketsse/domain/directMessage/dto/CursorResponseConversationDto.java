package com.mopl.moplwebsocketsse.domain.directMessage.dto;

import java.util.List;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;

public record CursorResponseConversationDto(
	List<ConversationDto> data,
	String nextCursor,
	String nextIdAfter,
	boolean hasNext,
	long totalCount,
	String sortBy,
	SortDirection sortDirection
) {
}
