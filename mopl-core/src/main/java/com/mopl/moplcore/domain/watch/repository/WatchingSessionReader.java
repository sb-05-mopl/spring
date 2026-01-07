package com.mopl.moplcore.domain.watch.repository;

import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WatchingSessionReader {
	private static final String CONTENT_WATCHERS_KEY_PREFIX = "watching:content:";

	private final StringRedisTemplate redisTemplate;

	public long countByContentId(UUID contentId) {
		String key = CONTENT_WATCHERS_KEY_PREFIX + contentId;
		Long count = redisTemplate.opsForZSet().zCard(key);
		return count != null ? count : 0L;
	}
}
