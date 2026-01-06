package com.mopl.moplwebsocketsse.domain.watch.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionSortBy;
import com.mopl.moplwebsocketsse.domain.watch.entity.WatchingSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WatchingSessionRepository {

	private final StringRedisTemplate stringRedisTemplate;

	private static final String CONTENT_WATCHERS_PREFIX = "watch:content:";
	private static final String SESSION_PREFIX = "session:";
	private static final String USER_WATCHING_PREFIX = "watch:user:";
	private static final int SESSION_TTL_SECONDS = 60;

	public void save(WatchingSession session) {
		String contentKey = CONTENT_WATCHERS_PREFIX + session.getContentId();
		String sessionKey = SESSION_PREFIX + session.getId();
		String userKey = USER_WATCHING_PREFIX + session.getWatcherId();

		stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
			@Override
			public List<Object> execute(RedisOperations operations) throws DataAccessException {
				operations.multi();

				// 1. 개별 세션 저장
				Map<String, String> sessionData = new LinkedHashMap<>();
				sessionData.put("userId", session.getWatcherId().toString());
				sessionData.put("contentId", session.getContentId().toString());
				sessionData.put("createdAt", String.valueOf(session.getCreatedAt().toEpochMilli()));

				operations.opsForHash().putAll(sessionKey, sessionData);
				operations.expire(sessionKey, SESSION_TTL_SECONDS, TimeUnit.SECONDS);

				// 2. 콘텐츠별 시청자 목록에 sessionId 추가
				double score = session.getCreatedAt().toEpochMilli();
				operations.opsForZSet().add(contentKey, session.getId().toString(), score);

				// 3. 역인덱스 (userId → sessionId)
				operations.opsForValue().set(userKey, session.getId().toString());
				operations.expire(userKey, SESSION_TTL_SECONDS, TimeUnit.SECONDS);

				return operations.exec();
			}
		});

		log.debug("[WatchingSession] Saved watching session. sessionId={}, userId={}, contentId={}",
			session.getId(), session.getWatcherId(), session.getContentId());
	}

	public void delete(UUID sessionId, UUID contentId, UUID watcherId) {
		String userKey = USER_WATCHING_PREFIX + watcherId;
		String currentSessionId = stringRedisTemplate.opsForValue().get(userKey);

		stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
			@Override
			public List<Object> execute(RedisOperations operations) {
				operations.multi();

				operations.delete(SESSION_PREFIX + sessionId);

				operations.opsForZSet().remove(
					CONTENT_WATCHERS_PREFIX + contentId,
					sessionId.toString()
				);

				if (sessionId.toString().equals(currentSessionId)) {
					operations.delete(userKey);
				}

				return operations.exec();
			}
		});

		log.debug("[WatchingSession] Deleted watching session. sessionId={}, userId={}, contentId={}",
			sessionId, watcherId, contentId);
	}

	/**
	 * 특정 콘텐츠의 시청 세션 목록 조회
	 * @param cursor 커서 (createdAt timestamp, null이면 처음부터)
	 * @param limit 조회 개수 (hasNext 판단을 위해 limit+1 조회)
	 */
	public List<WatchingSession> findSessionsByContentId(
		UUID contentId,
		Double cursor,
		UUID idAfter,
		int limit,
		WatchingSessionSortBy sortBy,
		SortDirection direction
	) {
		String contentKey = CONTENT_WATCHERS_PREFIX + contentId;

		List<WatchingSession> result = new ArrayList<>();
		Double currentCursor = cursor;
		String currentIdAfterStr = idAfter != null ? idAfter.toString() : null;

		int targetCount = limit + 1;
		int batchSize = 100;
		int maxIterations = 10;

		for (int i = 0; i < maxIterations && result.size() < targetCount; i++) {

			// 1. ZSET에서 배치 조회
			Set<ZSetOperations.TypedTuple<String>> tupleSet;

			if (direction == SortDirection.ASCENDING) {
				double minScore = currentCursor != null ? currentCursor : Double.NEGATIVE_INFINITY;
				tupleSet = stringRedisTemplate.opsForZSet().rangeByScoreWithScores(
					contentKey, minScore, Double.POSITIVE_INFINITY, 0, batchSize
				);
			} else {
				double maxScore = currentCursor != null ? currentCursor : Double.POSITIVE_INFINITY;
				tupleSet = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(
					contentKey, Double.NEGATIVE_INFINITY, maxScore, 0, batchSize
				);
			}

			if (tupleSet == null || tupleSet.isEmpty()) {
				break;
			}

			// Set → List 변환 (순서 유지, 마지막 접근 용이)
			List<ZSetOperations.TypedTuple<String>> tuples = new ArrayList<>(tupleSet);

			// 2. 커서 + 타이브레이커 필터링
			final Double filterCursor = currentCursor;
			final String filterIdAfter = currentIdAfterStr;

			List<ZSetOperations.TypedTuple<String>> filteredTuples = tuples.stream()
				.filter(tuple -> {
					if (filterCursor == null)
						return true;

					Double score = tuple.getScore();
					String sessionId = tuple.getValue();

					if (score == null || sessionId == null)
						return false;

					int scoreCompare = Double.compare(score, filterCursor);

					if (direction == SortDirection.ASCENDING) {
						if (scoreCompare > 0)
							return true;
						if (scoreCompare == 0 && filterIdAfter != null) {
							return sessionId.compareTo(filterIdAfter) > 0;
						}
						return false;
					} else {
						if (scoreCompare < 0)
							return true;
						if (scoreCompare == 0 && filterIdAfter != null) {
							return sessionId.compareTo(filterIdAfter) < 0;
						}
						return false;
					}
				})
				.toList();

			ZSetOperations.TypedTuple<String> lastTuple = tuples.getLast();
			currentCursor = lastTuple.getScore();
			currentIdAfterStr = lastTuple.getValue();

			// 필터링 후 비어있으면 다음 반복
			if (filteredTuples.isEmpty()) {
				// 원본도 batchSize보다 적으면 더 이상 없음
				if (tuples.size() < batchSize) {
					break;
				}
				continue;
			}

			List<String> sessionIds = filteredTuples.stream()
				.map(ZSetOperations.TypedTuple::getValue)
				.toList();

			// 3. 존재 여부 확인 (Pipeline)
			List<Object> existsResults = stringRedisTemplate.executePipelined(
				new SessionCallback<Object>() {
					@Override
					public Object execute(RedisOperations operations) {
						for (String sessionId : sessionIds) {
							operations.hasKey(SESSION_PREFIX + sessionId);
						}
						return null;
					}
				}
			);

			// 4. 존재하는 것과 유령 분리
			List<String> existingIds = new ArrayList<>();
			List<String> ghostIds = new ArrayList<>();

			for (int j = 0; j < sessionIds.size(); j++) {
				if (Boolean.TRUE.equals(existsResults.get(j))) {
					existingIds.add(sessionIds.get(j));
				} else {
					ghostIds.add(sessionIds.get(j));
				}
			}

			// 5. 유령 제거
			if (!ghostIds.isEmpty()) {
				stringRedisTemplate.opsForZSet().remove(contentKey, ghostIds.toArray());
			}

			// 6. 세션 상세 조회
			if (!existingIds.isEmpty()) {
				List<Object> hashResults = stringRedisTemplate.executePipelined(
					new SessionCallback<Object>() {
						@Override
						public Object execute(RedisOperations operations) {
							for (String sessionId : existingIds) {
								operations.opsForHash().entries(SESSION_PREFIX + sessionId);
							}
							return null;
						}
					}
				);

				// 7. WatchingSession 변환 & 누적
				for (int j = 0; j < existingIds.size() && result.size() < targetCount; j++) {
					String sessionId = existingIds.get(j);
					@SuppressWarnings("unchecked")
					Map<Object, Object> hash = (Map<Object, Object>)hashResults.get(j);

					String hashUserId = (String)hash.get("userId");
					String hashContentId = (String)hash.get("contentId");
					String hashCreatedAt = (String)hash.get("createdAt");

					if (hash.isEmpty() || hashUserId == null || hashContentId == null || hashCreatedAt == null) {
						continue;
					}

					result.add(WatchingSession.builder()
						.id(UUID.fromString(sessionId))
						.watcherId(UUID.fromString(hashUserId))
						.contentId(UUID.fromString(hashContentId))
						.createdAt(Instant.ofEpochMilli(Long.parseLong(hashCreatedAt)))
						.build());
				}
			}

			// 8. 원본 조회 결과가 batchSize보다 적으면 더 이상 없음
			if (tuples.size() < batchSize) {
				break;
			}
		}

		return result;
	}

	public WatchingSession findSessionByWatcherId(UUID watcherId) {
		String userKey = USER_WATCHING_PREFIX + watcherId;

		String sessionId = stringRedisTemplate.opsForValue().get(userKey);

		if (sessionId == null) {
			return null;
		}

		String sessionKey = SESSION_PREFIX + sessionId;
		Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries(sessionKey);

		String userId = (String)hash.get("userId");
		String contentId = (String)hash.get("contentId");
		String createdAtStr = (String)hash.get("createdAt");

		if (hash.isEmpty() || userId == null || contentId == null || createdAtStr == null) {
			String current = stringRedisTemplate.opsForValue().get(userKey);
			if (sessionId.equals(current)) {
				stringRedisTemplate.delete(userKey);
			}
			stringRedisTemplate.delete(sessionKey);
			return null;
		}

		return WatchingSession.builder()
			.id(UUID.fromString(sessionId))
			.watcherId(UUID.fromString(userId))
			.contentId(UUID.fromString(contentId))
			.createdAt(Instant.ofEpochMilli(Long.parseLong(createdAtStr)))
			.build();
	}

	public WatchingSession findSessionById(UUID sessionId) {
		String sessionKey = SESSION_PREFIX + sessionId;
		Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries(sessionKey);

		if (hash.isEmpty()) {
			return null;
		}

		String userId = (String)hash.get("userId");
		String contentId = (String)hash.get("contentId");
		String createdAt = (String)hash.get("createdAt");

		if (userId == null || contentId == null || createdAt == null) {
			return null;
		}

		return WatchingSession.builder()
			.id(sessionId)
			.watcherId(UUID.fromString(userId))
			.contentId(UUID.fromString(contentId))
			.createdAt(Instant.ofEpochMilli(Long.parseLong(createdAt)))
			.build();
	}

	public long countWatchersByContentId(UUID contentId) {
		Long count = stringRedisTemplate.opsForZSet().zCard(
			CONTENT_WATCHERS_PREFIX + contentId
		);
		return count != null ? count : 0L;
	}

	// 레디스에 Session TTL 갱신
	public boolean refreshSessionTtl(UUID sessionId, UUID watcherId) {
		Boolean sessionAlive = stringRedisTemplate.expire(
			SESSION_PREFIX + sessionId,
			SESSION_TTL_SECONDS,
			TimeUnit.SECONDS
		);

		stringRedisTemplate.expire(
			USER_WATCHING_PREFIX + watcherId,
			SESSION_TTL_SECONDS,
			TimeUnit.SECONDS
		);

		return Boolean.TRUE.equals(sessionAlive);
	}

	public List<Boolean> existsSessions(List<UUID> sessionIds) {
		if (sessionIds == null || sessionIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Object> results = stringRedisTemplate.executePipelined(
			new SessionCallback<Object>() {
				@Override
				public Object execute(RedisOperations operations) {
					for (UUID sessionId : sessionIds) {
						operations.hasKey(SESSION_PREFIX + sessionId);
					}
					return null;
				}
			}
		);

		List<Boolean> existsList = new ArrayList<>(results.size());
		for (Object r : results) {
			existsList.add(Boolean.TRUE.equals(r));
		}
		return existsList;
	}
}
