package com.mopl.moplcore.security.jwt.registry;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.mopl.moplcore.security.auth.dto.JwtInformation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Profile({"prod", "dev","local"})
@Component
@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

	private static final String ACCESS_PREFIX = "jwt:access:";
	private static final String REFRESH_PREFIX = "jwt:refresh:";
	private static final String USER_TOKENS_PREFIX = "jwt:user:tokens:";

	private final RedisTemplate<String, String> stringRedisTemplate;

	@Value("${jwt.access-token.expiration-ms}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-token.expiration-ms}")
	private long refreshTokenExpiration;

	@Value("${jwt.max-active}")
	private int maxActiveCount;

	@Override
	public void registerJwtInformation(JwtInformation jwtInformation) {
		UUID userId = jwtInformation.getUserDto().getId();
		String accessToken = jwtInformation.getAccessToken();
		String refreshToken = jwtInformation.getRefreshToken();

		stringRedisTemplate.opsForValue()
			.set(ACCESS_PREFIX + accessToken, userId.toString(), accessTokenExpiration, TimeUnit.MILLISECONDS);
		stringRedisTemplate.opsForValue()
			.set(REFRESH_PREFIX + refreshToken, userId.toString(), refreshTokenExpiration, TimeUnit.MILLISECONDS);

		String userTokensKey = USER_TOKENS_PREFIX + userId;

		Long tokenCount = stringRedisTemplate.opsForList().size(userTokensKey);
		if (tokenCount != null && tokenCount >= maxActiveCount) {
			String oldRefreshToken = stringRedisTemplate.opsForList().leftPop(userTokensKey);
			if (oldRefreshToken != null) {
				stringRedisTemplate.delete(REFRESH_PREFIX + oldRefreshToken);
			}
		}

		stringRedisTemplate.opsForList().rightPush(userTokensKey, refreshToken);
		stringRedisTemplate.expire(userTokensKey, refreshTokenExpiration, TimeUnit.MILLISECONDS);
	}

	@Override
	public void invalidateJwtInformationByUserId(UUID userId) {
		String userTokensKey = USER_TOKENS_PREFIX + userId;

		List<String> userRefreshTokens = stringRedisTemplate.opsForList().range(userTokensKey, 0, -1);
		if (userRefreshTokens != null) {
			userRefreshTokens.forEach(token -> stringRedisTemplate.delete(REFRESH_PREFIX + token));
		}

		stringRedisTemplate.delete(userTokensKey);
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
		UUID userId = newJwtInformation.getUserDto().getId();
		String userTokensKey = USER_TOKENS_PREFIX + userId;

		stringRedisTemplate.delete(REFRESH_PREFIX + oldRefreshToken);

		List<String> tokens = stringRedisTemplate.opsForList().range(userTokensKey, 0, -1);
		if (tokens != null) {
			for (int i = 0; i < tokens.size(); i++) {
				if (tokens.get(i).equals(oldRefreshToken)) {
					stringRedisTemplate.opsForList().set(userTokensKey, i, newJwtInformation.getRefreshToken());
					break;
				}
			}
		}

		registerJwtInformation(newJwtInformation);
	}

	@Override
	public void clearExpiredJwtInformation() {
		// 레디스는 TTL이 존재하기에 불필요.
	}
}