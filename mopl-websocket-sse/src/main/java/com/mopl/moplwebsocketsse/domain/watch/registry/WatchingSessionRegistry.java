package com.mopl.moplwebsocketsse.domain.watch.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.watch.repository.WatchingSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionRegistry {

	private final WatchingSessionRepository repository;

	private final Map<String, SessionMapping> subscriptionMappings = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> wsToSubscriptions = new ConcurrentHashMap<>();

	private String makeKey(String wsSessionId, String subscriptionId) {
		return wsSessionId + ":" + subscriptionId;
	}

	public void register(String wsSessionId, String subscriptionId,
		UUID watchingSessionId, UUID userId, UUID contentId) {
		SessionMapping mapping = new SessionMapping(
			wsSessionId, subscriptionId, watchingSessionId, userId, contentId
		);

		String key = makeKey(wsSessionId, subscriptionId);
		subscriptionMappings.put(key, mapping);
		wsToSubscriptions.computeIfAbsent(wsSessionId, k -> ConcurrentHashMap.newKeySet())
			.add(key);

		log.debug("[WatchingSessionRegistry] Registered. wsId={}, subId={}, key={}, watchingId={}",
			wsSessionId, subscriptionId, key, watchingSessionId);
	}

	public SessionMapping removeBySubscriptionId(String wsSessionId, String subscriptionId) {
		String key = makeKey(wsSessionId, subscriptionId);
		SessionMapping mapping = subscriptionMappings.remove(key);

		if (mapping != null) {
			Set<String> subs = wsToSubscriptions.get(mapping.webSocketSessionId());
			if (subs != null) {
				subs.remove(key);
			}
			log.debug("[WatchingSessionRegistry] Removed by key. key={}, watchingId={}",
				key, mapping.watchingSessionId());
		}

		return mapping;
	}

	public List<SessionMapping> removeAllByWsSessionId(String wsSessionId) {
		Set<String> keys = wsToSubscriptions.remove(wsSessionId);

		if (keys == null || keys.isEmpty()) {
			return Collections.emptyList();
		}

		List<SessionMapping> mappings = new ArrayList<>();
		for (String key : keys) {
			SessionMapping mapping = subscriptionMappings.remove(key);
			if (mapping != null) {
				mappings.add(mapping);
			}
		}

		log.debug("[WatchingSessionRegistry] Removed all by wsId. wsId={}, count={}", wsSessionId, mappings.size());
		return mappings;
	}

	public SessionMapping getBySubscriptionId(String wsSessionId, String subscriptionId) {
		return subscriptionMappings.get(makeKey(wsSessionId, subscriptionId));
	}

	public List<SessionMapping> getAllByWsSessionId(String wsSessionId) {
		Set<String> keys = wsToSubscriptions.get(wsSessionId);

		if (keys == null || keys.isEmpty()) {
			return Collections.emptyList();
		}

		List<SessionMapping> mappings = new ArrayList<>();
		for (String key : keys) {
			SessionMapping mapping = subscriptionMappings.get(key);
			if (mapping != null) {
				mappings.add(mapping);
			}
		}

		return mappings;
	}

	@Scheduled(fixedRate = 300000)
	public void cleanupRegistry() {
		if (subscriptionMappings.isEmpty())
			return;

		log.debug("[WatchingSessionRegistry] Cleanup started. size={}", subscriptionMappings.size());

		List<Map.Entry<String, SessionMapping>> entries =
			new ArrayList<>(subscriptionMappings.entrySet());

		List<UUID> sessionIds = entries.stream()
			.map(e -> e.getValue().watchingSessionId())
			.toList();

		List<Boolean> existsList = repository.existsSessions(sessionIds);

		int removedCount = 0;
		for (int i = 0; i < entries.size(); i++) {
			boolean exists = (i < existsList.size()) && Boolean.TRUE.equals(existsList.get(i));
			if (!exists) {
				String key = entries.get(i).getKey();
				SessionMapping mapping = subscriptionMappings.remove(key);
				if (mapping != null) {
					Set<String> subs = wsToSubscriptions.get(mapping.webSocketSessionId());
					if (subs != null) {
						subs.remove(key);
					}
					removedCount++;
				}
			}
		}

		if (removedCount > 0) {
			log.info("[WatchingSessionRegistry] Cleanup completed. removed={}", removedCount);
		}
	}
}