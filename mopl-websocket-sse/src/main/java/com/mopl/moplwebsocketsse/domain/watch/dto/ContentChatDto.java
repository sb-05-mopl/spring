package com.mopl.moplwebsocketsse.domain.watch.dto;

import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;

public record ContentChatDto(
	UserSummary sender,
	String content
) {
}
