package com.mopl.moplwebsocketsse.domain.watch.dto;

import java.time.Instant;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.content.dto.ContentSummary;
import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;

public record WatchingSessionDto(
	UUID id,
	Instant createdAt,
	UserSummary watcher,
	ContentSummary content
) {
}
