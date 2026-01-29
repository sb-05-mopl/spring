package com.mopl.moplwebsocketsse.domain.directMessage.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectMessageReadService {

	private final NotificationRepository notificationRepository;

	@Transactional
	public void clearDmNotifications(UUID receiverId, UUID conversationId) {
		notificationRepository.deleteDmNotificationsByConversationId(receiverId, conversationId);
	}
}
