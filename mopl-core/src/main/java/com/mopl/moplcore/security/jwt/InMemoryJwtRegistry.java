package com.mopl.moplcore.security.jwt;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mopl.moplcore.security.auth.dto.JwtInformation;
import com.mopl.moplcore.security.jwt.registry.JwtRegistry;
import com.mopl.moplcore.security.jwt.registry.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Profile({"local", "dev"})
@Component
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry {

	private final Map<UUID, Queue<JwtInformation>> session = new ConcurrentHashMap<>();

	private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
	private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();

	private final int maxActiveJwtCount = 1;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public void registerJwtInformation(JwtInformation jwtInformation) {
		session.compute(jwtInformation.getUserDto().getId(), (key, queue) -> {
			if (queue == null) {
				queue = new ConcurrentLinkedQueue<>();
			}
			if (queue.size() >= maxActiveJwtCount) {
				JwtInformation deprecatedJwtInformation = queue.poll();
				if (deprecatedJwtInformation != null) {
					removeTokenIndex(
						deprecatedJwtInformation.getAccessToken(),
						deprecatedJwtInformation.getRefreshToken()
					);
				}
			}

			queue.add(jwtInformation);
			addTokenIndex(
				jwtInformation.getAccessToken(),
				jwtInformation.getRefreshToken()
			);
			return queue;
		});
	}

	@Override
	public void invalidateJwtInformationByUserId(UUID userId) {
		session.computeIfPresent(userId, (key, queue) -> {
			queue.forEach(jwtInformation -> {
				removeTokenIndex(
					jwtInformation.getAccessToken(),
					jwtInformation.getRefreshToken()
				);
			});
			queue.clear();
			return null;
		});
	}

	@Override
	public boolean hasActiveJwtInformationByUserId(UUID userId) {
		return session.containsKey(userId);
	}

	@Override
	public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
		return accessToken != null && accessTokenIndexes.contains(accessToken);
	}

	@Override
	public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
		return refreshToken != null && refreshTokenIndexes.contains(refreshToken);
	}

	@Override
	public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
		session.computeIfPresent(newJwtInformation.getUserDto().getId(), (key, queue) -> {
			queue.stream().filter(jwtInformation -> jwtInformation.getRefreshToken().equals(refreshToken))
				.findFirst()
				.ifPresent(jwtInformation -> {
					removeTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
					jwtInformation.rotate(
						newJwtInformation.getAccessToken(),
						newJwtInformation.getRefreshToken()
					);
					addTokenIndex(
						newJwtInformation.getAccessToken(),
						newJwtInformation.getRefreshToken()
					);
				});
			return queue;
		});
	}

	@Scheduled(fixedDelay = 1000 * 60 * 5)
	@Override
	public void clearExpiredJwtInformation() {
		session.entrySet().removeIf(entry -> {
			Queue<JwtInformation> queue = entry.getValue();
			queue.removeIf(jwtInformation -> {
				boolean isExpired =
					!jwtTokenProvider.validateAccessToken(jwtInformation.getAccessToken())
						|| !jwtTokenProvider.validateRefreshToken(jwtInformation.getRefreshToken());
				if (isExpired) {
					removeTokenIndex(
						jwtInformation.getAccessToken(),
						jwtInformation.getRefreshToken()
					);
				}
				return isExpired;
			});
			return queue.isEmpty();
		});
	}

	private void addTokenIndex(String accessToken, String refreshToken) {
		if (accessToken != null && !accessToken.isBlank()) {
			accessTokenIndexes.add(accessToken);
		}
		if (refreshToken != null && !refreshToken.isBlank()) {
			refreshTokenIndexes.add(refreshToken);
		}
	}

	private void removeTokenIndex(String accessToken, String refreshToken) {
		if (accessToken != null && !accessToken.isBlank()) {
			accessTokenIndexes.remove(accessToken);
		}
		if (refreshToken != null && !refreshToken.isBlank()) {
			refreshTokenIndexes.remove(refreshToken);
		}
	}
}
