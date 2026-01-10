package com.mopl.moplcore.domain.user.dto;

import java.util.List;
import java.util.UUID;

public record CursorResponseUserDto(
	List<UserResponse> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	long totalCount,
	String sortBy,
	String sortDirection
) {

}
