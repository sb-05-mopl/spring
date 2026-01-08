package com.mopl.moplwebsocketsse.domain.notification.repository;

import java.util.List;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortBy;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortDirection;
import com.mopl.moplwebsocketsse.domain.notification.entity.Notification;

public interface NotificationRepositoryCustom {

	List<Notification> findByReceiverIdWithCursor(
		UUID receiverId,
		boolean unreadOnly,
		String cursor,
		UUID idAfter,
		int limit,
		NotificationSortBy sortBy,
		NotificationSortDirection sortDirection
	);
}
