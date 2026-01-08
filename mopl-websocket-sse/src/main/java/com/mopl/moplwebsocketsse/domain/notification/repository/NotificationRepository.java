package com.mopl.moplwebsocketsse.domain.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplwebsocketsse.domain.notification.entity.Notification;

public interface NotificationRepository
	extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

	long countByReceiverId(UUID receiverId);

	long countByReceiverIdAndReadAtIsNull(UUID receiverId);
}
