package com.mopl.moplwebsocketsse.security.registry;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.security.dto.JwtInformation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile({"prod", "dev", "local"})
@Component
@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

	private static final String ACCESS_PREFIX = "jwt:access:";
	private static final String REFRESH_PREFIX = "jwt:refresh:";
	private static final String USER_TOKENS_PREFIX = "jwt:user:tokens:";

	private final RedisTemplate<String, String> stringRedisTemplate;


	@Override
	public void registerJwtInformation(JwtInformation jwtInformation) {
	}

	@Override
	public void invalidateJwtInformationByUserId(UUID userId) {
	}

	@Override
	public boolean hasActiveJwtInformationByUserId(UUID userId) {
		return stringRedisTemplate.hasKey(USER_TOKENS_PREFIX + userId);
	}

	@Override
	public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
		return stringRedisTemplate.hasKey(ACCESS_PREFIX + accessToken);
	}

	@Override
	public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
		return stringRedisTemplate.hasKey(REFRESH_PREFIX + refreshToken);
	}

	@Override
	public void rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation) {

	}

	@Override
	public void clearExpiredJwtInformation() {
		// 레디스는 TTL이 존재하기에 불필요.
	}
}