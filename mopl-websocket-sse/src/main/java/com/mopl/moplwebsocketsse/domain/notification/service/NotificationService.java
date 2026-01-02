package com.mopl.moplwebsocketsse.domain.notification.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.entity.Notification;
import com.mopl.moplwebsocketsse.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	@Transactional(readOnly = true)
	public List<NotificationDto> list(UUID me, boolean unreadOnly) {
		List<Notification> list = unreadOnly
			? notificationRepository.findByReceiverIdAndReadAtIsNullOrderByCreatedAtDesc(me)
			: notificationRepository.findByReceiverIdOrderByCreatedAtDesc(me);

		return list.stream().map(NotificationDto::from).toList();
	}

	@Transactional
	public NotificationDto markAsRead(UUID me, UUID notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new IllegalArgumentException("Notification not found : " + notificationId));

		if (!notification.getReceiverId().equals(me)) {
			throw new AccessDeniedException("You are not allowed to read this notification : " + notificationId);
		}

		notification.markAsRead();
		return NotificationDto.from(notification);
	}
}
