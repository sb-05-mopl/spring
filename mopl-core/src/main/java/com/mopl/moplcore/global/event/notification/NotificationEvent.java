package com.mopl.moplcore.global.event.notification;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationEvent(
	UUID eventId,
	Instant createdAt,
	UUID receiverId,
	NotificationLevel level,
	NotificationEventType type,
	Map<String, String> meta
) {
}
