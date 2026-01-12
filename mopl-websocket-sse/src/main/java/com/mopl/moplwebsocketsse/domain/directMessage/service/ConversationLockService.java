package com.mopl.moplwebsocketsse.domain.directMessage.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationCreatedLockResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationLockService {

	private final StringRedisTemplate redisTemplate;

	private static final String LOCK_PREFIX = "lock:conversation:";
	private static final Duration TTL = Duration.ofSeconds(30);

	private static final String UNLOCK_SCRIPT = """
		if redis.call('get', KEYS[1]) == ARGV[1] then
		    return redis.call('del', KEYS[1])
		else
		    return 0
		end
		""";

	public Optional<ConversationCreatedLockResult> tryLock(UUID userId1, UUID userId2) {
		String key = generateKey(userId1, userId2);
		String value = UUID.randomUUID().toString();

		Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, TTL);
		boolean acquired = Boolean.TRUE.equals(result);

		log.debug("[ConversationLock] tryLock key={}, acquired={}", key, acquired);

		return acquired ? Optional.of(new ConversationCreatedLockResult(key, value)) : Optional.empty();
	}

	public void unlock(String key, String expectedValue) {
		Long result = redisTemplate.execute(
			new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class),
			List.of(key),
			expectedValue
		);

		log.debug("[ConversationLock] unlock key={}, success={}", key, result != null && result > 0);
	}

	private String generateKey(UUID userId1, UUID userId2) {
		String id1 = userId1.toString();
		String id2 = userId2.toString();

		String smaller = id1.compareTo(id2) < 0 ? id1 : id2;
		String larger = id1.compareTo(id2) < 0 ? id2 : id1;

		return LOCK_PREFIX + smaller + ":" + larger;
	}
}