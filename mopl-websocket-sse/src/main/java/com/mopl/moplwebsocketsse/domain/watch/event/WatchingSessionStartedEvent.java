package com.mopl.moplwebsocketsse.domain.watch.event;

import java.time.Instant;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

public record WatchingSessionStartedEvent(
	UUID eventId,
	Instant createdAt,
	UUID actorId,
	String actorName,
	UUID contentId,
	String contentTitle,
	UUID watchingSessionId
) {
	public static WatchingSessionStartedEvent of(
		UUID actorId,
		String actorName,
		UUID contentId,
		String contentTitle,
		UUID watchingSessionID
	) {
		return new WatchingSessionStartedEvent(
			UuidCreator.getTimeOrderedEpoch(),
			Instant.now(),
			actorId,
			actorName,
			contentId,
			contentTitle,
			watchingSessionID
		);
	}
}
