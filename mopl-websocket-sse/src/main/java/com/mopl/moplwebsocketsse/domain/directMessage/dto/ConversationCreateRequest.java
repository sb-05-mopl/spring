package com.mopl.moplwebsocketsse.domain.directMessage.dto;

import java.util.UUID;

public record ConversationCreateRequest(
	UUID withUserId
) {
}
