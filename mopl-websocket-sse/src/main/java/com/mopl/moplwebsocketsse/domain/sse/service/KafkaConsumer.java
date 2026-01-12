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

	private final SseService sseService;

	@Value("${mopl.kafka.topics.notifications}")
	private String notificationsTopic;

	@Value("${mopl.kafka.topics.direct-messages}")
	private String directMessagesTopic;

	@KafkaListener(topics = "#{__listener.notificationsTopic}", groupId = "mopl-websocket-sse")
	public void onNotification(String message, Acknowledgment acknowledgement) {
		try {
			NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
			notificationEventHandler.handle(event);
			acknowledgement.acknowledge();
		} catch (Exception e) {
			log.error("Failed to consume notification message: {}", message, e);
		}
	}

	// 임시 코드
	@KafkaListener(topics = "#{__listener.directMessagesTopic}", groupId = "mopl-websocket-sse")
	public void onDirectMessages(String message, Acknowledgment acknowledgement) {
		//		try {
		//			DirectMessageDto dto = objectMapper.readValue(message, DirectMessageDto.class);
		//			UUID receiverId = dto.getReceiverId();
		//			sseService.broadcast(receiverId, "direct-messages", dto.id().toString(), dto);
		//			acknowledgement.acknowledge();
		//		} catch (Exception e) {
		//			log.error("Failed to consume direct-messages message: {}", message, e);
		//		}
	}
}
