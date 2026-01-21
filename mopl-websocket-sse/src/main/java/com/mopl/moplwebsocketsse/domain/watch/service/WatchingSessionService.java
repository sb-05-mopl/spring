package com.mopl.moplwebsocketsse.domain.watch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;
import com.mopl.moplwebsocketsse.domain.content.dto.ContentSummary;
import com.mopl.moplwebsocketsse.domain.content.entity.Content;
import com.mopl.moplwebsocketsse.domain.content.entity.Tag;
import com.mopl.moplwebsocketsse.domain.content.exception.ContentNotFoundException;
import com.mopl.moplwebsocketsse.domain.content.repository.ContentRepository;
import com.mopl.moplwebsocketsse.domain.content.repository.ContentTagRepository;
import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;
import com.mopl.moplwebsocketsse.domain.user.entity.User;
import com.mopl.moplwebsocketsse.domain.user.exception.UserNotFoundException;
import com.mopl.moplwebsocketsse.domain.user.repository.UserRepository;
import com.mopl.moplwebsocketsse.domain.watch.dto.CursorResponseWatchingSessionDto;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionChange;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionDto;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionSearchRequest;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionSortBy;
import com.mopl.moplwebsocketsse.domain.watch.entity.WatchingSession;
import com.mopl.moplwebsocketsse.domain.watch.repository.WatchingSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WatchingSessionService {

	private final WatchingSessionRepository watchingSessionRepository;
	private final UserRepository userRepository;
	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;
	private final MeterRegistry meterRegistry;

	public WatchingSessionChange createJoinMessage(WatchingSession watchingSession) {
		UserSummary userSummary = getUserSummary(watchingSession.getWatcherId());
		ContentSummary contentSummary = getContentSummary(watchingSession.getContentId());

		WatchingSessionDto dto = new WatchingSessionDto(
			watchingSession.getId(),
			watchingSession.getCreatedAt(),
			userSummary,
			contentSummary
		);

		long watcherCount = watchingSessionRepository.countWatchersByContentId(watchingSession.getContentId());

		return new WatchingSessionChange(WatchingSessionChange.ChangeType.JOIN, dto, watcherCount);
	}

	public WatchingSessionChange createLeaveMessage(UUID watchingSessionId, UUID contentId) {
		WatchingSession session = watchingSessionRepository.findSessionById(watchingSessionId);

		if (session == null) {
			log.warn("WatchingSession not found. sessionId={}", watchingSessionId);
			return createMinimalLeaveMessage(watchingSessionId, contentId);
		}

		UserSummary userSummary = getUserSummary(session.getWatcherId());
		ContentSummary contentSummary = getContentSummary(session.getContentId());

		WatchingSessionDto dto = new WatchingSessionDto(
			session.getId(),
			session.getCreatedAt(),
			userSummary,
			contentSummary
		);

		long watcherCount = watchingSessionRepository.countWatchersByContentId(contentId) - 1;

		return new WatchingSessionChange(
			WatchingSessionChange.ChangeType.LEAVE,
			dto,
			Math.max(0, watcherCount)
		);
	}

	public CursorResponseWatchingSessionDto findSessionsByContent(
		UUID contentId,
		WatchingSessionSearchRequest request
	) {
		Double cursor = request.cursor() != null ? Double.parseDouble(request.cursor()) : null;
		UUID idAfter = request.idAfter();
		int limit = request.limit();
		WatchingSessionSortBy sortBy = request.sortBy();
		SortDirection sortDirection = request.sortDirection();
		String watcherNameLike = request.watcherNameLike();

		Timer.Sample redisSessionSample = Timer.start(meterRegistry);
		List<WatchingSession> sessions = watchingSessionRepository.findSessionsByContentId(
			contentId,
			cursor,
			idAfter,
			limit,
			sortBy,
			sortDirection
		);
		redisSessionSample.stop(meterRegistry.timer("watching.session.redis.find"));

		boolean hasNext;
		String nextCursor = null;
		UUID nextIdAfter = null;
		List<WatchingSession> actualSessions;

		if (sessions.size() > limit) {
			hasNext = true;
			actualSessions = sessions.subList(0, limit);
			WatchingSession lastSession = actualSessions.getLast();
			nextCursor = String.valueOf(lastSession.getCreatedAt().toEpochMilli());
			nextIdAfter = lastSession.getId();
		} else {
			actualSessions = sessions;
			if (!sessions.isEmpty()) {
				WatchingSession lastSession = sessions.getLast();
				double lastScore = lastSession.getCreatedAt().toEpochMilli();

				Timer.Sample redisHasMoreSample = Timer.start(meterRegistry);
				hasNext = watchingSessionRepository.hasMoreAfter(contentId, lastScore, sortDirection);
				redisHasMoreSample.stop(meterRegistry.timer("watching.session.redis.hasmore"));

				if (hasNext) {
					nextCursor = String.valueOf(lastSession.getCreatedAt().toEpochMilli());
					nextIdAfter = lastSession.getId();
				}
			} else {
				hasNext = false;
			}
		}

		// 1. User 배치 조회
		List<UUID> watcherIds = actualSessions.stream()
			.map(WatchingSession::getWatcherId)
			.distinct()
			.toList();

		Timer.Sample dbUserSample = Timer.start(meterRegistry);
		Map<UUID, User> userMap = userRepository.findAllById(watcherIds).stream()
			.collect(Collectors.toMap(User::getId, Function.identity()));
		dbUserSample.stop(meterRegistry.timer("watching.session.db.user"));

		// 2. Content 단일 조회
		Timer.Sample dbContentSample = Timer.start(meterRegistry);
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> ContentNotFoundException.withContentId(contentId));
		dbContentSample.stop(meterRegistry.timer("watching.session.db.content"));

		// 3. Tag 단일 조회 (같은 contentId)
		Timer.Sample dbTagSample = Timer.start(meterRegistry);
		List<String> tags = contentTagRepository.findTagsByContentId(contentId).stream()
			.map(Tag::getName)
			.toList();
		dbTagSample.stop(meterRegistry.timer("watching.session.db.tag"));

		ContentSummary contentSummary = ContentSummary.builder()
			.id(content.getId())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(content.getThumbnailUrl())
			.tags(tags)
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.build();

		// 4. DTO 변환
		List<WatchingSessionDto> dtos = new ArrayList<>();
		for (WatchingSession session : actualSessions) {
			User user = userMap.get(session.getWatcherId());
			if (user == null) continue;

			// watcherNameLike 필터링
			if (watcherNameLike != null && !user.getName().contains(watcherNameLike)) {
				continue;
			}

			UserSummary userSummary = new UserSummary(
				user.getId(),
				user.getName(),
				user.getProfileImageUrl()
			);

			WatchingSessionDto dto = new WatchingSessionDto(
				session.getId(),
				session.getCreatedAt(),
				userSummary,
				contentSummary
			);
			dtos.add(dto);
		}

		Timer.Sample redisCountSample = Timer.start(meterRegistry);
		long totalCount = watchingSessionRepository.countWatchersByContentId(contentId);
		redisCountSample.stop(meterRegistry.timer("watching.session.redis.count"));

		return new CursorResponseWatchingSessionDto(
			dtos,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			sortBy,
			sortDirection
		);
	}

	public WatchingSessionDto findSessionByWatcher(UUID watcherId) {
		WatchingSession session = watchingSessionRepository.findSessionByWatcherId(watcherId);

		if (session == null) {
			return null;
		}

		UserSummary userSummary = getUserSummary(session.getWatcherId());
		ContentSummary contentSummary = getContentSummary(session.getContentId());

		return new WatchingSessionDto(
			session.getId(),
			session.getCreatedAt(),
			userSummary,
			contentSummary
		);
	}

	private UserSummary getUserSummary(UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> UserNotFoundException.withUserId(userId));

		return new UserSummary(user.getId(), user.getName(), user.getProfileImageUrl());
	}

	private ContentSummary getContentSummary(UUID contentId) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> ContentNotFoundException.withContentId(contentId));

		List<Tag> tagEntities = contentTagRepository.findTagsByContentId(contentId);

		List<String> tagNames = tagEntities.stream()
			.map(Tag::getName)
			.toList();

		return ContentSummary.builder()
			.id(content.getId())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(content.getThumbnailUrl())
			.tags(tagNames)
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.build();
	}

	private WatchingSessionChange createMinimalLeaveMessage(UUID sessionId, UUID contentId) {
		WatchingSessionDto dto = new WatchingSessionDto(
			sessionId,
			java.time.Instant.now(),
			new UserSummary(UUID.randomUUID(), "Unknown", null),
			getContentSummary(contentId)
		);

		long watcherCount = watchingSessionRepository.countWatchersByContentId(contentId);

		return new WatchingSessionChange(
			WatchingSessionChange.ChangeType.LEAVE,
			dto,
			watcherCount
		);
	}
}
