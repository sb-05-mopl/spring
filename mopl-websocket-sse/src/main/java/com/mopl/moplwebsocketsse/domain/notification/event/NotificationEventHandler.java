package com.mopl.moplwebsocketsse.domain.notification.event;

import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.service.NotificationService;
import com.mopl.moplwebsocketsse.domain.sse.service.SseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

	private final NotificationService notificationService;
	private final SseService sseService;

	public void handle(NotificationEvent event) {
		if (!dedupeStore.firstProcess(event.eventId())) {
			return;
		}

		NotificationDto saved = notificationService.createFromEvent(event);
		sseService.broadcast(event.receiverId(), "notifications", event.eventId().toString(), saved);
	}
}
