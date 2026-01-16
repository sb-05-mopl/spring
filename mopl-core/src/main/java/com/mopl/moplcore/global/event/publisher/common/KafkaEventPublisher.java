package com.mopl.moplcore.global.event.publisher.common;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public void publish(String topic, String key, String jsonPayload) {
		kafkaTemplate.send(topic, key, jsonPayload);
	}
}