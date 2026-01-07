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

	public void register(String wsSessionId, String subscriptionId,
		UUID watchingSessionId, UUID userId, UUID contentId) {
		SessionMapping mapping = new SessionMapping(
			wsSessionId, subscriptionId, watchingSessionId, userId, contentId
		);

		subscriptionMappings.put(subscriptionId, mapping);
		wsToSubscriptions.computeIfAbsent(wsSessionId, k -> ConcurrentHashMap.newKeySet())
			.add(subscriptionId);

		log.debug("Registered. wsId={}, subId={}, watchingId={}",
			wsSessionId, subscriptionId, watchingSessionId);
	}

	public SessionMapping removeBySubscriptionId(String subscriptionId) {
		SessionMapping mapping = subscriptionMappings.remove(subscriptionId);

		if (mapping != null) {
			Set<String> subs = wsToSubscriptions.get(mapping.webSocketSessionId());
			if (subs != null) {
				subs.remove(subscriptionId);
			}
			log.debug("Removed by subId. subId={}, watchingId={}",
				subscriptionId, mapping.watchingSessionId());
		}

		return mapping;
	}

	public List<SessionMapping> removeAllByWsSessionId(String wsSessionId) {
		Set<String> subscriptionIds = wsToSubscriptions.remove(wsSessionId);

		if (subscriptionIds == null || subscriptionIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<SessionMapping> mappings = new ArrayList<>();
		for (String subId : subscriptionIds) {
			SessionMapping mapping = subscriptionMappings.remove(subId);
			if (mapping != null) {
				mappings.add(mapping);
			}
		}

		log.debug("Removed all by wsId. wsId={}, count={}", wsSessionId, mappings.size());
		return mappings;
	}

	public SessionMapping getBySubscriptionId(String subscriptionId) {
		return subscriptionMappings.get(subscriptionId);
	}

	public List<SessionMapping> getAllByWsSessionId(String wsSessionId) {
		Set<String> subscriptionIds = wsToSubscriptions.get(wsSessionId);

		if (subscriptionIds == null || subscriptionIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<SessionMapping> mappings = new ArrayList<>();
		for (String subId : subscriptionIds) {
			SessionMapping mapping = subscriptionMappings.get(subId);
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

		List<Map.Entry<String, SessionMapping>> entries =
			new ArrayList<>(subscriptionMappings.entrySet());

		List<UUID> sessionIds = entries.stream()
			.map(e -> e.getValue().watchingSessionId())
			.toList();

		List<Boolean> existsList = repository.existsSessions(sessionIds);

		for (int i = 0; i < entries.size(); i++) {
			boolean exists = (i < existsList.size()) && Boolean.TRUE.equals(existsList.get(i));
			if (!exists) {
				String subId = entries.get(i).getKey();
				SessionMapping mapping = subscriptionMappings.remove(subId);
				if (mapping != null) {
					Set<String> subs = wsToSubscriptions.get(mapping.webSocketSessionId());
					if (subs != null) {
						subs.remove(subId);
					}
				}
			}
		}
	}
}