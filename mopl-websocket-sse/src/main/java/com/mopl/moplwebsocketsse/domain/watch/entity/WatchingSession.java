package com.mopl.moplwebsocketsse.domain.watch.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import com.github.f4b6a3.uuid.UuidCreator;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "watchingSession", timeToLive = 60)
public class WatchingSession {

	@Id
	private UUID id;

	@Indexed
	private UUID watcherId;

	@Indexed
	private UUID contentId;

	private Instant createdAt;

	@Builder
	public WatchingSession(UUID watcherId, UUID contentId) {
		this.id = UuidCreator.getTimeOrderedEpoch();
		this.watcherId = watcherId;
		this.contentId = contentId;
		this.createdAt = Instant.now();
	}
}