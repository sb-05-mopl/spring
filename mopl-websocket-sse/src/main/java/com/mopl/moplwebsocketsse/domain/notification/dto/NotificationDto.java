package com.mopl.moplwebsocketsse.domain.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.notification.entity.Notification;
import com.mopl.moplwebsocketsse.domain.notification.entity.NotificationLevel;

public record NotificationDto(
	UUID id,
	Instant createdAt,
	UUID receiverId,
	String title,
	String content,
	NotificationLevel level
) {
	public static NotificationDto from(Notification notification) {
		return new NotificationDto(
			notification.getId(),
			notification.getCreatedAt(),
			notification.getReceiverId(),
			notification.getTitle(),
			notification.getContent(),
			notification.getLevel()
		);
	}
}
