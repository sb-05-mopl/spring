package com.mopl.moplwebsocketsse.domain.watch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;
import com.mopl.moplwebsocketsse.domain.content.dto.ContentSummary;
import com.mopl.moplwebsocketsse.domain.content.entity.Content;
import com.mopl.moplwebsocketsse.domain.content.entity.Tag;
import com.mopl.moplwebsocketsse.domain.content.repository.ContentRepository;
import com.mopl.moplwebsocketsse.domain.content.repository.ContentTagRepository;
import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;
import com.mopl.moplwebsocketsse.domain.user.entity.User;
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
		// cursor를 Double로 변환 (createdAt timestamp)
		Double cursor = request.cursor() != null ? Double.parseDouble(request.cursor()) : null;
		UUID idAfterStr = request.idAfter();
		int limit = request.limit();
		WatchingSessionSortBy sortBy = request.sortBy();
		SortDirection sortDirection = request.sortDirection();
		String watcherNameLike = request.watcherNameLike();

		List<WatchingSession> sessions = watchingSessionRepository.findSessionsByContentId(
			contentId,
			cursor,
			idAfterStr,
			limit,
			sortBy,
			sortDirection
		);

		// hasNext 판단 (Repository에서 limit+1 조회했으므로)
		boolean hasNext = sessions.size() > limit;
		List<WatchingSession> actualSessions = hasNext ? sessions.subList(0, (int)limit) : sessions;

		List<WatchingSessionDto> dtos = new ArrayList<>();
		for (WatchingSession session : actualSessions) {
			UserSummary userSummary = getUserSummary(session.getWatcherId());

			// watcherNameLike 필터링
			// API 명세서에는 NameLike가 있는데 Redis는 Like를 처리할 수없어서 임시로
			if (watcherNameLike != null && !userSummary.name().contains(watcherNameLike)) {
				continue;
			}

			ContentSummary contentSummary = getContentSummary(session.getContentId());

			WatchingSessionDto dto = new WatchingSessionDto(
				session.getId(),
				session.getCreatedAt(),
				userSummary,
				contentSummary
			);
			dtos.add(dto);
		}

		String nextCursor = null;
		UUID nextIdAfter = null;
		if (hasNext && !actualSessions.isEmpty()) {
			WatchingSession lastSession = actualSessions.getLast();
			nextCursor = String.valueOf(lastSession.getCreatedAt().toEpochMilli());
			nextIdAfter = lastSession.getId();
		}

		long totalCount = watchingSessionRepository.countWatchersByContentId(contentId);

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
			.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		return new UserSummary(user.getId(), user.getName(), user.getProfileImageUrl());
	}

	private ContentSummary getContentSummary(UUID contentId) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new IllegalArgumentException("Content not found: " + contentId));

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
