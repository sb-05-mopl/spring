package com.mopl.moplwebsocketsse.domain.notification.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mopl.moplwebsocketsse.domain.notification.entity.Notification;

public interface NotificationRepository
	extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

	long countByReceiverId(UUID receiverId);

	long deleteByIdAndReceiverId(UUID id, UUID receiverId);

	Optional<Notification> findByIdAndReceiverId(UUID id, UUID receiverId);

	Optional<Notification> findByEventIdAndReceiverId(UUID eventId, UUID receiverId);

	boolean existsByEventId(UUID eventId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		    delete from Notification n
		    where n.receiverId = :receiverId
		      and n.eventId in (
		          select dm.id
		          from DirectMessage dm
		          where dm.conversation.id = :conversationId
		      )
		""")
	int deleteDmNotificationsByConversationId(
		@Param("receiverId") UUID receiverId,
		@Param("conversationId") UUID conversationId
	);
}
