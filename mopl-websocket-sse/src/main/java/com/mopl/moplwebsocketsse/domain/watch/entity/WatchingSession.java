package com.mopl.moplwebsocketsse.domain.watch.entity;

import java.time.Instant;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingSession {

	private UUID id;

	private UUID watcherId;

	private UUID contentId;

	private Instant createdAt;

	public WatchingSession(UUID watcherId, UUID contentId) {
		this.id = UuidCreator.getTimeOrderedEpoch();
		this.watcherId = watcherId;
		this.contentId = contentId;
		this.createdAt = Instant.now();
	}

	@Builder
	public WatchingSession(UUID id, UUID watcherId, UUID contentId, Instant createdAt) {
		this.id = id;
		this.watcherId = watcherId;
		this.contentId = contentId;
		this.createdAt = createdAt;
	}
}