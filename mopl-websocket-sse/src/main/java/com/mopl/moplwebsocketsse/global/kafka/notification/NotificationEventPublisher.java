package com.mopl.moplwebsocketsse.global.kafka.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplwebsocketsse.domain.notification.event.NotificationEvent;
import com.mopl.moplwebsocketsse.global.kafka.common.KafkaEventPublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

	private final KafkaEventPublisher kafkaEventPublisher;
	private final ObjectMapper objectMapper;

	@Value("${mopl.kafka.topics.notifications}")
	private String notificationsTopic;

	public void publish(NotificationEvent event) {
		try {
			String json = objectMapper.writeValueAsString(event);
			kafkaEventPublisher.publish(
				notificationsTopic,
				event.receiverId().toString(),
				json
			);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to publish NotificationEvent: " + event, e);
		}
	}
}
