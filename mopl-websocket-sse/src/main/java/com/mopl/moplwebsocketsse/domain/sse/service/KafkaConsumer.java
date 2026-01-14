package com.mopl.moplwebsocketsse.domain.sse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplwebsocketsse.domain.notification.event.NotificationEvent;
import com.mopl.moplwebsocketsse.domain.notification.event.NotificationEventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

	private final ObjectMapper objectMapper;
	private final NotificationEventHandler notificationEventHandler;

	@Value("${mopl.kafka.topics.notifications}")
	private String notificationsTopic;

	@KafkaListener(topics = "#{__listener.notificationsTopic}", groupId = "mopl-websocket-sse")
	public void onNotification(String message, Acknowledgment acknowledgement) throws Exception {
		NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
		notificationEventHandler.handle(event);
		acknowledgement.acknowledge();
	}
}
