package com.mopl.moplwebsocketsse.domain.directMessage.registry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class DirectMessageSubscriptionRegistry {

	private final Map<UUID, Map<UUID, Integer>> active = new ConcurrentHashMap<>();

	public void subscribe(UUID conversationId, UUID userId) {
		active.compute(conversationId, (cid, users) -> {
			if (users == null)
				users = new ConcurrentHashMap<>();
			users.merge(userId, 1, Integer::sum);
			return users;
		});
	}

	public void unsubscribe(UUID conversationId, UUID userId) {
		active.computeIfPresent(conversationId, (cid, users) -> {
			Integer count = users.get(userId);
			if (count == null)
				return users;

			if (count <= 1)
				users.remove(userId);
			else
				users.put(userId, count - 1);

			return users.isEmpty() ? null : users;
		});
	}

	public boolean isSubscribed(UUID conversationId, UUID userId) {
		Map<UUID, Integer> users = active.get(conversationId);
		return users != null && users.getOrDefault(userId, 0) > 0;
	}
}
