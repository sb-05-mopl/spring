package com.mopl.moplwebsocketsse.domain.watch.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WatchingSessionRegistry {

	private final Map<String, SessionMapping> subscriptionMappings = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> wsToSubscriptions = new ConcurrentHashMap<>();

	private String makeKey(String wsSessionId, String subscriptionId) {
		return wsSessionId + ":" + subscriptionId;
	}

	public void register(String wsSessionId, String subscriptionId,
		UUID watchingSessionId, UUID userId, UUID contentId,
		WebSocketSession webSocketSession) {

		SessionMapping mapping = new SessionMapping(
			wsSessionId, subscriptionId, watchingSessionId, userId, contentId, webSocketSession
		);

		String key = makeKey(wsSessionId, subscriptionId);
		subscriptionMappings.put(key, mapping);
		wsToSubscriptions.computeIfAbsent(wsSessionId, k -> ConcurrentHashMap.newKeySet())
			.add(key);

		log.debug("[WatchingSessionRegistry] Registered. wsId={}, subId={}, watchingId={}",
			wsSessionId, subscriptionId, watchingSessionId);
	}

	public void updateLastActiveTimeByWsSessionId(String wsSessionId) {
		Set<String> keys = wsToSubscriptions.get(wsSessionId);
		if (keys == null || keys.isEmpty()) {
			return;
		}

		for (String key : keys) {
			SessionMapping mapping = subscriptionMappings.get(key);
			if (mapping != null) {
				mapping.updateLastActiveTime();
			}
		}
	}

	public Collection<SessionMapping> getAllMappings() {
		return Collections.unmodifiableCollection(subscriptionMappings.values());
	}

	public SessionMapping removeBySubscriptionId(String wsSessionId, String subscriptionId) {
		String key = makeKey(wsSessionId, subscriptionId);
		SessionMapping mapping = subscriptionMappings.remove(key);

		if (mapping != null) {
			Set<String> subs = wsToSubscriptions.get(mapping.getWebSocketSessionId());
			if (subs != null) {
				subs.remove(key);
			}
			log.debug("[WatchingSessionRegistry] Removed. key={}, watchingId={}",
				key, mapping.getWatchingSessionId());
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

		log.debug("[WatchingSessionRegistry] Removed all. wsId={}, count={}", wsSessionId, mappings.size());
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

	public int size() {
		return subscriptionMappings.size();
	}
}