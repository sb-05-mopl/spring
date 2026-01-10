package com.mopl.moplcore.domain.user.registry;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisUserRegistry implements UserRegistry {

	private final RedisTemplate<String, String> redisTemplate;
	private final PasswordEncoder passwordEncoder;

	private static final String KEY_PREFIX = "temp:password:";
	private static final Duration TTL = Duration.ofMinutes(3);

	@Override
	public String setTempPassword(UUID userId) {
		String originPassword = UUID.randomUUID().toString().substring(0, 8);
		String encodedPassword = passwordEncoder.encode(originPassword);

		String key = getKey(userId);
		redisTemplate.opsForValue().set(key, encodedPassword, TTL);

		return originPassword;
	}

	@Override
	public String getEncodedPassword(UUID userId) {
		String key = getKey(userId);
		return redisTemplate.opsForValue().get(key);
	}

	@Override
	public boolean existById(UUID userId) {
		String key = getKey(userId);
		return redisTemplate.hasKey(key);
	}

	@Override
	public void removeTempPassword(UUID userId) {
		String key = getKey(userId);
		redisTemplate.delete(key);
	}

	private String getKey(UUID userId) {
		return KEY_PREFIX + userId.toString();
	}
}