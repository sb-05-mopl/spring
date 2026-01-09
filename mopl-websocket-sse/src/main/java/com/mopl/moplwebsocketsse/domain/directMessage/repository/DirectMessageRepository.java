package com.mopl.moplwebsocketsse.domain.directMessage.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mopl.moplwebsocketsse.domain.directMessage.entity.DirectMessage;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID>, DirectMessageRepositoryCustom {
	@Query("""
    SELECT dm.conversation.id FROM DirectMessage dm
    WHERE dm.conversation.id IN :conversationIds
      AND dm.sender.id != :userId
      AND dm.readAt IS NULL
    GROUP BY dm.conversation.id
    """)
	List<UUID> findConversationIdsWithUnreadMessages(List<UUID> conversationIds, UUID userId);
}
