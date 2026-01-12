package com.mopl.moplwebsocketsse.domain.notification.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.notification.entity.NotificationLevel;

public record NotificationEvent(
	UUID eventId,
	Instant createdAt,
	UUID receiverId,
	NotificationLevel level,
	NotificationEventType type,
	Map<String, Object> meta
) {
}
