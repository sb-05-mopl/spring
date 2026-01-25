package com.mopl.moplcore.global.event.watch;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionStartedEvent(
	UUID eventId,
	Instant createdAt,
	UUID actorId,
	String actorName,
	UUID contentId,
	String contentTitle,
	UUID watchingSessionId
) {
}
