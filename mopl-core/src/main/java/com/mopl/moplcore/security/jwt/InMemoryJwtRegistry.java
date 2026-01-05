package com.mopl.moplcore.security.jwt;

import com.mopl.moplcore.domain.auth.dto.JwtInformation;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry {

  private final JwtTokenProvider jwtTokenProvider;

  // 유저당 1세션(활성 토큰 묶음)
  private final Map<UUID, JwtInformation> activeByUserId = new ConcurrentHashMap<>();

  // 토큰 문자열로 활성 여부 확인하기 위한 인덱스
  private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
  private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();

  // refreshToken -> userId (rotate에 필요)
  private final Map<String, UUID> userIdByRefreshToken = new ConcurrentHashMap<>();

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.getUserDto().id();

    // 유저당 1세션: 기존 세션 무효화 후 새로 등록
    invalidateJwtInformationByUserId(userId);

    activeByUserId.put(userId, jwtInformation);
    addTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
    userIdByRefreshToken.put(jwtInformation.getRefreshToken(), userId);
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    JwtInformation removed = activeByUserId.remove(userId);
    if (removed == null) {
      return;
    }

    removeTokenIndex(removed.getAccessToken(), removed.getRefreshToken());
    userIdByRefreshToken.remove(removed.getRefreshToken());
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    return activeByUserId.containsKey(userId);
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
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }

    UUID userId = userIdByRefreshToken.get(refreshToken);
    if (userId == null) {
      // 활성 refresh가 아님
      return;
    }

    // 기존 정보 제거
    JwtInformation old = activeByUserId.get(userId);
    if (old != null) {
      removeTokenIndex(old.getAccessToken(), old.getRefreshToken());
      userIdByRefreshToken.remove(old.getRefreshToken());
    }

    // 새 정보 등록
    activeByUserId.put(userId, newJwtInformation);
    addTokenIndex(newJwtInformation.getAccessToken(), newJwtInformation.getRefreshToken());
    userIdByRefreshToken.put(newJwtInformation.getRefreshToken(), userId);
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    // activeByUserId를 순회하며 refresh가 만료/무효면 제거
    for (Map.Entry<UUID, JwtInformation> entry : activeByUserId.entrySet()) {
      UUID userId = entry.getKey();
      JwtInformation info = entry.getValue();

      String refreshToken = info.getRefreshToken();
      if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
        invalidateJwtInformationByUserId(userId);
      }
    }
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
