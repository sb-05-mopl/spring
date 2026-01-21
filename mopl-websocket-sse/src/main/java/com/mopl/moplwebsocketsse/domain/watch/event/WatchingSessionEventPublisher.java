package com.mopl.moplwebsocketsse.domain.watch.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplwebsocketsse.global.publisher.common.KafkaEventPublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatchingSessionEventPublisher {

	private final KafkaEventPublisher kafkaEventPublisher;
	private final ObjectMapper objectMapper;

	@Value("${mopl.kafka.topics.watching-session-started}")
	private String topic;

	public void publish(WatchingSessionStartedEvent event) {
		try {
			String json = objectMapper.writeValueAsString(event);
			kafkaEventPublisher.publish(topic, event.actorId().toString(), json);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to publish WatchingSessionStartedEvent : " + event, e);
		}
	}
}
