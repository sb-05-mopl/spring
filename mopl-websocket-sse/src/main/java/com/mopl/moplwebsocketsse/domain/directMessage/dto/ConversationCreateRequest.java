package com.mopl.moplwebsocketsse.domain.directMessage.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ConversationCreateRequest(
	@NotNull UUID withUserId
) {
}
