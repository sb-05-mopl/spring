package com.mopl.moplwebsocketsse.security.jwt.registry;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

	private static final String ACCESS_PREFIX = "jwt:access:";

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
		return stringRedisTemplate.hasKey(ACCESS_PREFIX + accessToken);
	}
}