package com.mopl.moplwebsocketsse.domain.directMessage.repository;

import java.util.List;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;

public interface ConversationRepositoryCustom {
	List<Conversation> findByParticipantIdWithCursor(
		UUID participantId,
		String keywordLike,
		String cursor,
		UUID idAfter,
		int limit,
		String sortDirection
	);

	long countByParticipantId(UUID participantId, String keywordLike);
}
