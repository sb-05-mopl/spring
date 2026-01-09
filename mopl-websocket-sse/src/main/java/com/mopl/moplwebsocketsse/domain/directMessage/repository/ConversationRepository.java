package com.mopl.moplwebsocketsse.domain.directMessage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID>, ConversationRepositoryCustom {
	@Query("""
    SELECT c FROM Conversation c
    LEFT JOIN FETCH c.lastMessage lm
    LEFT JOIN FETCH lm.sender
    WHERE c.id = :conversationId
    """)
	Optional<Conversation> findByIdWithLastMessage(UUID conversationId);

	@Query("""
    SELECT c FROM Conversation c
    LEFT JOIN FETCH c.lastMessage lm
    LEFT JOIN FETCH lm.sender
    WHERE c.id IN :conversationIds
    """)
	List<Conversation> findByIdInWithLastMessage(List<UUID> conversationIds);
}
