package com.mopl.moplwebsocketsse.domain.notification.dto;

import java.util.List;
import java.util.UUID;

public record CursorResponseNotificationDto<T>(
	List<T> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	long totalCount,
	NotificationSortBy sortBy,
	NotificationSortDirection sortDirection
) {
}
