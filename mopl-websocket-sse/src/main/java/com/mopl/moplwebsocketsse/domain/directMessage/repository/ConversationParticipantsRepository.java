package com.mopl.moplwebsocketsse.domain.directMessage.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.ConversationParticipants;

public interface ConversationParticipantsRepository extends JpaRepository<ConversationParticipants, UUID> {
	@Query("""
		SELECT cp1.conversation FROM ConversationParticipants cp1
		JOIN ConversationParticipants cp2 ON cp1.conversation = cp2.conversation
		WHERE cp1.user.id = :userId1 AND cp2.user.id = :userId2
		""")
	Optional<Conversation> findConversationBetween(UUID userId1, UUID userId2);

	@Query("""
		SELECT cp FROM ConversationParticipants cp
		JOIN FETCH cp.user
		WHERE cp.conversation.id = :conversationId
		""")
	List<ConversationParticipants> findByConversationIdWithUser(UUID conversationId);

	@Query("""
		SELECT cp FROM ConversationParticipants cp
		JOIN FETCH cp.user
		WHERE cp.conversation.id IN :conversationIds
		""")
	List<ConversationParticipants> findByConversationIdInWithUser(List<UUID> conversationIds);

	boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

	@Modifying
	@Transactional
	void deleteByConversationId(UUID conversationId);
}
