package com.mopl.moplwebsocketsse.domain.directMessage.dto;

import java.time.Instant;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;

public record DirectMessageDto(
	UUID id,
	UUID conversationId,
	Instant createdAt,
	UserSummary sender,
	UserSummary receiver,
	String content
) {
}
