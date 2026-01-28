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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;
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
	private static final int FETCH_MULTIPLIER = 3;

	public void save(WatchingSession session) {
		String contentKey = CONTENT_WATCHERS_PREFIX + session.getContentId();
		String sessionKey = SESSION_PREFIX + session.getId();
		String userKey = USER_WATCHING_PREFIX + session.getWatcherId();

		String oldSessionId = stringRedisTemplate.opsForValue().get(userKey);
		if (oldSessionId != null) {
			String oldSessionKey = SESSION_PREFIX + oldSessionId;
			String oldContentId = (String) stringRedisTemplate.opsForHash().get(oldSessionKey, "contentId");

			if (oldContentId != null) {
				stringRedisTemplate.opsForZSet().remove(
					CONTENT_WATCHERS_PREFIX + oldContentId,
					oldSessionId
				);
			}
			stringRedisTemplate.delete(oldSessionKey);

			log.debug("[WatchingSession] Cleaned up old session. oldSessionId={}, oldContentId={}",
				oldSessionId, oldContentId);
		}

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
	 * - 유령 데이터 대비 limit * 3 조회 후 유효 데이터만 반환
	 * - 반복 없이 1회 조회
	 *
	 * @param cursor 커서 (createdAt timestamp, null이면 처음부터)
	 * @param limit 요청 개수
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

		int fetchSize = limit * FETCH_MULTIPLIER;

		// 1. ZSET에서 조회
		Set<ZSetOperations.TypedTuple<String>> tupleSet;

		if (direction == SortDirection.ASCENDING) {
			double minScore = cursor != null ? cursor : Double.NEGATIVE_INFINITY;
			tupleSet = stringRedisTemplate.opsForZSet().rangeByScoreWithScores(
				contentKey, minScore, Double.POSITIVE_INFINITY, 0, fetchSize
			);
		} else {
			double maxScore = cursor != null ? cursor : Double.POSITIVE_INFINITY;
			tupleSet = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(
				contentKey, Double.NEGATIVE_INFINITY, maxScore, 0, fetchSize
			);
		}

		if (tupleSet == null || tupleSet.isEmpty()) {
			return Collections.emptyList();
		}

		// Set → List 변환
		List<ZSetOperations.TypedTuple<String>> tuples = new ArrayList<>(tupleSet);

		// 2. 커서 + 타이브레이커 필터링
		final Double filterCursor = cursor;
		final String filterIdAfter = idAfter != null ? idAfter.toString() : null;

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

		if (filteredTuples.isEmpty()) {
			return Collections.emptyList();
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

		for (int i = 0; i < sessionIds.size(); i++) {
			if (Boolean.TRUE.equals(existsResults.get(i))) {
				existingIds.add(sessionIds.get(i));
			} else {
				ghostIds.add(sessionIds.get(i));
			}
		}

		// 5. 유령 제거
		if (!ghostIds.isEmpty()) {
			stringRedisTemplate.opsForZSet().remove(contentKey, ghostIds.toArray());
		}

		// 6. 유효 세션이 없으면 빈 리스트 반환
		if (existingIds.isEmpty()) {
			return Collections.emptyList();
		}

		// 7. 세션 상세 조회 (Pipeline)
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

		// 8. WatchingSession 변환
		List<WatchingSession> result = new ArrayList<>();
		for (int i = 0; i < existingIds.size(); i++) {
			String sessionId = existingIds.get(i);
			Map<Object, Object> hash = (Map<Object, Object>)hashResults.get(i);

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

		return result;
	}

	public boolean hasMoreAfter(UUID contentId, double score, SortDirection direction) {
		String contentKey = CONTENT_WATCHERS_PREFIX + contentId;

		Long count;
		if (direction == SortDirection.ASCENDING) {
			count = stringRedisTemplate.opsForZSet()
				.count(contentKey, score + 0.001, Double.POSITIVE_INFINITY);
		} else {
			count = stringRedisTemplate.opsForZSet()
				.count(contentKey, Double.NEGATIVE_INFINITY, score - 0.001);
		}

		return count != null && count > 0;
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

	public List<Boolean> batchRefreshSessionTtl(List<UUID> sessionIds, List<UUID> watcherIds) {
		if (sessionIds == null || sessionIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Object> results = stringRedisTemplate.executePipelined(
			new SessionCallback<Object>() {
				@Override
				public Object execute(RedisOperations operations) {
					for (int i = 0; i < sessionIds.size(); i++) {
						UUID sessionId = sessionIds.get(i);
						UUID watcherId = watcherIds.get(i);

						operations.expire(SESSION_PREFIX + sessionId, SESSION_TTL_SECONDS, TimeUnit.SECONDS);
						operations.expire(USER_WATCHING_PREFIX + watcherId, SESSION_TTL_SECONDS, TimeUnit.SECONDS);
					}
					return null;
				}
			}
		);

		List<Boolean> sessionResults = new ArrayList<>();
		for (int i = 0; i < results.size(); i += 2) {
			sessionResults.add(Boolean.TRUE.equals(results.get(i)));
		}

		return sessionResults;
	}

	public int cleanupGhostEntries() {
		int totalRemoved = 0;

		ScanOptions scanKeysOptions = ScanOptions.scanOptions()
			.match(CONTENT_WATCHERS_PREFIX + "*")
			.count(100)
			.build();

		try (Cursor<String> keysCursor = stringRedisTemplate.scan(scanKeysOptions)) {
			while (keysCursor.hasNext()) {
				String contentKey = keysCursor.next();

				ScanOptions memberScanOptions = ScanOptions.scanOptions()
					.count(100)
					.build();

				try (Cursor<ZSetOperations.TypedTuple<String>> membersCursor =
						 stringRedisTemplate.opsForZSet().scan(contentKey, memberScanOptions)) {

					List<String> batch = new ArrayList<>();

					while (membersCursor.hasNext()) {
						ZSetOperations.TypedTuple<String> tuple = membersCursor.next();
						if (tuple.getValue() != null) {
							batch.add(tuple.getValue());
						}

						if (batch.size() >= 100 || !membersCursor.hasNext()) {
							if (batch.isEmpty()) continue;

							List<String> currentBatch = new ArrayList<>(batch);
							batch.clear();

							List<Object> existsResults = stringRedisTemplate.executePipelined(
								new SessionCallback<Object>() {
									@Override
									public Object execute(RedisOperations operations) {
										for (String sessionId : currentBatch) {
											operations.hasKey(SESSION_PREFIX + sessionId);
										}
										return null;
									}
								}
							);

							List<String> ghosts = new ArrayList<>();
							for (int i = 0; i < currentBatch.size(); i++) {
								if (!Boolean.TRUE.equals(existsResults.get(i))) {
									ghosts.add(currentBatch.get(i));
								}
							}

							if (!ghosts.isEmpty()) {
								stringRedisTemplate.opsForZSet().remove(contentKey, ghosts.toArray());
								totalRemoved += ghosts.size();
							}
						}
					}
				}
			}
		}

		return totalRemoved;
	}
}