package com.mopl.moplwebsocketsse.domain.watch.scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.mopl.moplwebsocketsse.domain.watch.registry.SessionMapping;
import com.mopl.moplwebsocketsse.domain.watch.registry.WatchingSessionRegistry;
import com.mopl.moplwebsocketsse.domain.watch.repository.WatchingSessionRepository;
import com.mopl.moplwebsocketsse.security.registry.JwtRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionBatchRefresher {

	private static final CloseStatus SESSION_EXPIRED = new CloseStatus(4001, "Session expired");

	private final WatchingSessionRegistry registry;
	private final WatchingSessionRepository repository;
	private final JwtRegistry jwtRegistry;

	@Scheduled(fixedRate = 30_000)
	public void refreshActiveSessions() {
		Collection<SessionMapping> mappings = registry.getAllMappings();

		if (mappings.isEmpty()) {
			return;
		}

		log.debug("[BatchRefresher] Starting batch refresh. sessionCount={}", mappings.size());

		List<SessionMapping> validMappings = new ArrayList<>();
		List<SessionMapping> invalidMappings = new ArrayList<>();

		for (SessionMapping mapping : mappings) {
			try {
				boolean hasJwt = jwtRegistry.hasActiveJwtInformationByUserId(mapping.getUserId());
				if (hasJwt) {
					validMappings.add(mapping);
				} else {
					invalidMappings.add(mapping);
				}
			} catch (Exception e) {
				log.warn("[BatchRefresher] JWT check failed, treating as invalid. userId={}",
					mapping.getUserId(), e);
				invalidMappings.add(mapping);
			}
		}

		if (!validMappings.isEmpty()) {
			List<UUID> sessionIds = validMappings.stream()
				.map(SessionMapping::getWatchingSessionId)
				.toList();
			List<UUID> watcherIds = validMappings.stream()
				.map(SessionMapping::getUserId)
				.toList();

			try {
				repository.batchRefreshSessionTtl(sessionIds, watcherIds);
				log.debug("[BatchRefresher] Refreshed TTL for {} sessions", validMappings.size());
			} catch (Exception e) {
				log.error("[BatchRefresher] Failed to refresh TTL", e);
			}
		}

		if (!invalidMappings.isEmpty()) {
			log.info("[BatchRefresher] Cleaning up {} invalid sessions", invalidMappings.size());

			for (SessionMapping mapping : invalidMappings) {
				closeWebSocketSession(mapping);

				registry.removeBySubscriptionId(
					mapping.getWebSocketSessionId(),
					mapping.getSubscriptionId()
				);

				log.info("[BatchRefresher] Removed invalid session. wsId={}, userId={}, watchingId={}",
					mapping.getWebSocketSessionId(), mapping.getUserId(), mapping.getWatchingSessionId());
			}
		}

		log.debug("[BatchRefresher] Batch refresh completed. valid={}, invalid={}",
			validMappings.size(), invalidMappings.size());
	}

	private void closeWebSocketSession(SessionMapping mapping) {
		WebSocketSession session = mapping.getWebSocketSession();

		if (session == null) {
			return;
		}

		if (!session.isOpen()) {
			return;
		}

		try {
			session.close(SESSION_EXPIRED);
			log.debug("[BatchRefresher] Closed WebSocket session. wsId={}", mapping.getWebSocketSessionId());
		} catch (IOException e) {
			log.warn("[BatchRefresher] Failed to close WebSocket session. wsId={}",
				mapping.getWebSocketSessionId(), e);
		}
	}
}