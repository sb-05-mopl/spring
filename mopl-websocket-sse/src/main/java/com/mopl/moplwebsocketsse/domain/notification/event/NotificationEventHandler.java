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
	private final NotificationMessageBuilder messageBuilder;

	public void handle(NotificationEvent event) {
		NotificationMetaSpec.validate(event);

		NotificationMessageBuilder.Message msg = messageBuilder.build(event);

		NotificationDto saved = notificationService.createFromEvent(event, msg.title(), msg.content());

		sseService.broadcast(
			event.receiverId(),
			"notifications",
			event.eventId().toString(),
			saved
		);
	}
}
