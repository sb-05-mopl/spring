package com.mopl.moplcore.domain.watch.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatchingSessionReader {
	private static final String CONTENT_WATCHERS_KEY_PREFIX = "watch:content:";

	private final StringRedisTemplate redisTemplate;

	public long countByContentId(UUID contentId) {
		String key = CONTENT_WATCHERS_KEY_PREFIX + contentId;
		Long count = redisTemplate.opsForZSet().zCard(key);
		return count != null ? count : 0L;
	}

	public Map<UUID, Long> countByContentIds(List<UUID> contentIds) {
		if (contentIds == null || contentIds.isEmpty()) {
			return Map.of();
		}

		List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
			@Override
			@SuppressWarnings("unchecked")
			public Object execute(RedisOperations operations) throws DataAccessException {
				for (UUID contentId : contentIds) {
					String key = CONTENT_WATCHERS_KEY_PREFIX + contentId;
					operations.opsForZSet().zCard(key);
				}
				return null;
			}
		});

		Map<UUID, Long> watcherCountMap = new HashMap<>();
		for (int i = 0; i < contentIds.size(); i++) {
			UUID contentId = contentIds.get(i);
			Long count = (Long) results.get(i);
			watcherCountMap.put(contentId, count != null ? count : 0L);
		}

		return watcherCountMap;
	}
}
