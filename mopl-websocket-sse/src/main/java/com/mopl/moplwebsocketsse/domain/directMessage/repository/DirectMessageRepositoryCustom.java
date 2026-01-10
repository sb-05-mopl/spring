package com.mopl.moplwebsocketsse.domain.directMessage.repository;

import java.util.List;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.directMessage.entity.DirectMessage;

public interface DirectMessageRepositoryCustom {
	List<DirectMessage> findByConversationIdWithCursor(
		UUID conversationId,
		String cursor,
		UUID idAfter,
		int limit,
		String sortDirection
	);

	long countByConversationId(UUID conversationId);
}
