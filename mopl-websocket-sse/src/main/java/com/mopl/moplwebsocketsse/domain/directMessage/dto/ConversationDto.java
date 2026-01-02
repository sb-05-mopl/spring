package com.mopl.moplwebsocketsse.domain.directMessage.dto;

import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;

public record ConversationDto(
	UUID uuid,
	UserSummary with,
	DirectMessageDto lastMessage,
	boolean hasUnread
) {
}
