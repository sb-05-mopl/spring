package com.mopl.moplwebsocketsse.domain.directMessage.dto;

import jakarta.validation.constraints.NotBlank;

public record DirectMessageSendRequest(
	@NotBlank String content
) {
}
