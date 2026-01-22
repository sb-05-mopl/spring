package com.mopl.moplcore.domain.playlist.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.content.dto.ContentSummary;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.Type;
import com.mopl.moplcore.domain.content.exception.ContentNotFoundException;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentTagRepository;
import com.mopl.moplcore.domain.follow.activity.FolloweeActivityKind;
import com.mopl.moplcore.domain.follow.activity.FolloweeActivityNotifier;
import com.mopl.moplcore.domain.playlist.dto.CursorResponsePlaylistDto;
import com.mopl.moplcore.domain.playlist.dto.PlaylistCreateRequest;
import com.mopl.moplcore.domain.playlist.dto.PlaylistDto;
import com.mopl.moplcore.domain.playlist.dto.PlaylistSearchRequest;
import com.mopl.moplcore.domain.playlist.dto.PlaylistUpdateRequest;
import com.mopl.moplcore.domain.playlist.entity.Playlist;
import com.mopl.moplcore.domain.playlist.entity.PlaylistContent;
import com.mopl.moplcore.domain.playlist.entity.PlaylistSubscription;
import com.mopl.moplcore.domain.playlist.exception.PlaylistNotFoundException;
import com.mopl.moplcore.domain.playlist.repository.PlaylistContentRepository;
import com.mopl.moplcore.domain.playlist.repository.PlaylistRepository;
import com.mopl.moplcore.domain.playlist.repository.PlaylistSubscriptionRepository;
import com.mopl.moplcore.domain.user.dto.UserSummary;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.repository.UserRepository;
import com.mopl.moplcore.global.event.notification.NotificationEvent;
import com.mopl.moplcore.global.event.notification.NotificationEventFactory;
import com.mopl.moplcore.global.event.notification.NotificationMetaSpec;
import com.mopl.moplcore.global.event.publisher.notification.NotificationEventPublisher;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

	private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

	private final PlaylistRepository playlistRepository;
	private final PlaylistContentRepository playlistContentRepository;
	private final PlaylistSubscriptionRepository playlistSubscriptionRepository;
	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;
	private final UserRepository userRepository;
	private final NotificationEventPublisher notificationEventPublisher;
	private final FolloweeActivityNotifier followeeActivityNotifier;
	private final MeterRegistry meterRegistry;

	@Transactional
	public PlaylistDto createPlaylist(UUID userId, PlaylistCreateRequest request) {
		User owner = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

		Playlist playlist = new Playlist(owner, request.title(), request.description());
		Playlist savedPlaylist = playlistRepository.save(playlist);

		followeeActivityNotifier.notifyFollowers(
			owner.getId(),
			FolloweeActivityKind.PLAYLIST_CREATED,
			savedPlaylist.getId(),
			Map.of(
				NotificationMetaSpec.ACTOR_NAME, owner.getName(),
				NotificationMetaSpec.PLAYLIST_TITLE, savedPlaylist.getTitle(),
				NotificationMetaSpec.PLAYLIST_DESCRIPTION, savedPlaylist.getDescription()
			)
		);

		return toDtoSingle(savedPlaylist, userId);
	}

	public CursorResponsePlaylistDto searchPlaylists(PlaylistSearchRequest request, UUID currentUserId) {
		Timer.Sample mainSample = Timer.start(meterRegistry);
		List<Playlist> playlists = playlistRepository.searchPlaylists(request);
		mainSample.stop(Timer.builder("playlist.search.repository.main").register(meterRegistry));

		Timer.Sample countSample = Timer.start(meterRegistry);
		Long totalCount = playlistRepository.countPlaylists(request);
		countSample.stop(Timer.builder("playlist.search.repository.count").register(meterRegistry));

		boolean hasNext = playlists.size() > request.getLimit();
		if (hasNext) {
			playlists = playlists.subList(0, request.getLimit());
		}

		List<UUID> playlistIds = playlists.stream()
			.map(Playlist::getId)
			.toList();

		Timer.Sample subscriberCountSample = Timer.start(meterRegistry);
		Map<UUID, Long> countMap = playlistSubscriptionRepository
			.countByPlaylistIds(playlistIds)
			.stream()
			.collect(Collectors.toMap(
				arr -> (UUID) arr[0],
				arr -> (Long) arr[1]
			));
		subscriberCountSample.stop(Timer.builder("playlist.search.subscriberCount").register(meterRegistry));

		Timer.Sample subscribedByMeSample = Timer.start(meterRegistry);
		Set<UUID> subscribedIds = currentUserId != null
			? new HashSet<>(playlistSubscriptionRepository.findSubscribedPlaylistIds(playlistIds, currentUserId))
			: Collections.emptySet();
		subscribedByMeSample.stop(Timer.builder("playlist.search.subscribedByMe").register(meterRegistry));

		Timer.Sample contentsSample = Timer.start(meterRegistry);
		Map<UUID, List<PlaylistContent>> contentsMap = playlistContentRepository
			.findByPlaylistIds(playlistIds)
			.stream()
			.collect(Collectors.groupingBy(pc -> pc.getPlaylist().getId()));
		contentsSample.stop(Timer.builder("playlist.search.contents").register(meterRegistry));

		List<UUID> contentIds = contentsMap.values().stream()
			.flatMap(List::stream)
			.map(pc -> pc.getContent().getId())
			.toList();

		Timer.Sample tagsSample = Timer.start(meterRegistry);
		Map<UUID, List<String>> tagsMap = contentIds.isEmpty()
			? Collections.emptyMap()
			: contentTagRepository.findByContentIds(contentIds)
			.stream()
			.collect(Collectors.groupingBy(
				ct -> ct.getContent().getId(),
				Collectors.mapping(ct -> ct.getTag().getName(), Collectors.toList())
			));
		tagsSample.stop(Timer.builder("playlist.search.tags").register(meterRegistry));

		Timer.Sample mappingSample = Timer.start(meterRegistry);
		List<PlaylistDto> playlistDtos = playlists.stream()
			.map(playlist -> toDto(playlist, countMap, subscribedIds, contentsMap, tagsMap))
			.toList();
		mappingSample.stop(Timer.builder("playlist.search.mapping").register(meterRegistry));

		String nextCursor = null;
		if (hasNext && !playlists.isEmpty()) {
			Playlist lastPlaylist = playlists.get(playlists.size() - 1);
			nextCursor = encodeCursor(lastPlaylist.getId(), lastPlaylist.getUpdatedAt());
		}

		return CursorResponsePlaylistDto.builder()
			.data(playlistDtos)
			.nextCursor(nextCursor)
			.nextIdAfter(hasNext && !playlists.isEmpty() ? playlists.get(playlists.size() - 1).getId() : null)
			.hasNext(hasNext)
			.totalCount(totalCount)
			.sortBy(request.getSortBy().name())
			.sortDirection(request.getSortDirection().name())
			.build();
	}

	public PlaylistDto getPlaylist(UUID playlistId, UUID currentUserId) {
		Playlist playlist = playlistRepository.findById(playlistId)
			.orElseThrow(() -> new PlaylistNotFoundException(playlistId));

		return toDtoSingle(playlist, currentUserId);
	}

	@Transactional
	public PlaylistDto updatePlaylist(UUID playlistId, UUID userId, PlaylistUpdateRequest request) {
		Playlist playlist = playlistRepository.findById(playlistId)
			.orElseThrow(() -> new PlaylistNotFoundException(playlistId));

		if (!playlist.getOwner().getId().equals(userId)) {
			throw new IllegalArgumentException("플레이리스트 소유자만 수정할 수 있습니다");
		}

		playlist.update(request.title(), request.description());

		return toDtoSingle(playlist, userId);
	}

	@Transactional
	public void deletePlaylist(UUID playlistId, UUID userId) {
		Playlist playlist = playlistRepository.findById(playlistId)
			.orElseThrow(() -> new PlaylistNotFoundException(playlistId));

		if (!playlist.getOwner().getId().equals(userId)) {
			throw new IllegalArgumentException("플레이리스트 소유자만 삭제할 수 있습니다");
		}

		playlistRepository.delete(playlist);
	}

	@Transactional
	public void addContentToPlaylist(UUID playlistId, UUID contentId, UUID userId) {
		Playlist playlist = playlistRepository.findById(playlistId)
			.orElseThrow(() -> new PlaylistNotFoundException(playlistId));

		if (!playlist.getOwner().getId().equals(userId)) {
			throw new IllegalArgumentException("플레이리스트 소유자만 콘텐츠를 추가할 수 있습니다");
		}

		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new ContentNotFoundException(contentId));

		if (playlistContentRepository.findByPlaylistIdAndContentId(playlistId, contentId).isPresent()) {
			throw new IllegalArgumentException("이미 플레이리스트에 추가된 콘텐츠입니다");
		}

		PlaylistContent playlistContent = new PlaylistContent(playlist, content);
		playlistContentRepository.save(playlistContent);

		List<UUID> subscriberIds = playlistSubscriptionRepository.findSubscriberIdsByPlaylistId(playlistId);

		for (UUID receiverId : subscriberIds) {
			NotificationEvent event = NotificationEventFactory.subscribedPlaylistContentAdded(
				receiverId,
				playlistId,
				contentId,
				playlist.getTitle(),
				content.getTitle()
			);

			NotificationMetaSpec.validate(event);
			notificationEventPublisher.publish(event);
		}
	}

	@Transactional
	public void removeContentFromPlaylist(UUID playlistId, UUID contentId, UUID userId) {
		Playlist playlist = playlistRepository.findById(playlistId)
			.orElseThrow(() -> new PlaylistNotFoundException(playlistId));

		if (!playlist.getOwner().getId().equals(userId)) {
			throw new IllegalArgumentException("플레이리스트 소유자만 콘텐츠를 삭제할 수 있습니다");
		}

		playlistContentRepository.deleteByPlaylistIdAndContentId(playlistId, contentId);
	}

	@Transactional
	public void subscribePlaylist(UUID playlistId, UUID userId) {
		Playlist playlist = playlistRepository.findById(playlistId)
			.orElseThrow(() -> new PlaylistNotFoundException(playlistId));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

		if (playlistSubscriptionRepository.existsByPlaylistIdAndUserId(playlistId, userId)) {
			throw new IllegalArgumentException("이미 구독한 플레이리스트입니다");
		}

		PlaylistSubscription subscription = new PlaylistSubscription(playlist, user);
		playlistSubscriptionRepository.save(subscription);

		UUID receiverId = playlist.getOwner().getId();

		if (!receiverId.equals(user.getId())) {
			NotificationEvent event = NotificationEventFactory.playlistSubscribed(
				receiverId,
				playlistId,
				user.getId(),
				playlist.getTitle(),
				user.getName()
			);

			NotificationMetaSpec.validate(event);
			notificationEventPublisher.publish(event);
		}
	}

	@Transactional
	public void unsubscribePlaylist(UUID playlistId, UUID userId) {
		playlistSubscriptionRepository.deleteByPlaylistIdAndUserId(playlistId, userId);
	}

	private PlaylistDto toDto(
		Playlist playlist,
		Map<UUID, Long> countMap,
		Set<UUID> subscribedIds,
		Map<UUID, List<PlaylistContent>> contentsMap,
		Map<UUID, List<String>> tagsMap
	) {
		UserSummary owner = new UserSummary(
			playlist.getOwner().getId(),
			playlist.getOwner().getName(),
			playlist.getOwner().getProfileImageUrl()
		);

		Long subscriberCount = countMap.getOrDefault(playlist.getId(), 0L);

		Boolean subscribedByMe = subscribedIds.contains(playlist.getId());

		List<ContentSummary> contents = contentsMap
			.getOrDefault(playlist.getId(), Collections.emptyList())
			.stream()
			.map(pc -> toContentSummary(pc.getContent(), tagsMap))
			.toList();

		LocalDateTime updatedAtLocal = LocalDateTime.ofInstant(
			playlist.getUpdatedAt(),
			java.time.ZoneId.systemDefault()
		);

		return PlaylistDto.builder()
			.id(playlist.getId())
			.owner(owner)
			.title(playlist.getTitle())
			.description(playlist.getDescription())
			.updatedAt(updatedAtLocal)
			.subscriberCount(subscriberCount)
			.subscribedByMe(subscribedByMe)
			.contents(contents)
			.build();
	}

	private PlaylistDto toDtoSingle(Playlist playlist, UUID currentUserId) {
		List<UUID> playlistIds = List.of(playlist.getId());

		Map<UUID, Long> countMap = playlistSubscriptionRepository
			.countByPlaylistIds(playlistIds)
			.stream()
			.collect(Collectors.toMap(
				arr -> (UUID) arr[0],
				arr -> (Long) arr[1]
			));

		Set<UUID> subscribedIds = currentUserId != null
			? new HashSet<>(playlistSubscriptionRepository.findSubscribedPlaylistIds(playlistIds, currentUserId))
			: Collections.emptySet();

		Map<UUID, List<PlaylistContent>> contentsMap = playlistContentRepository
			.findByPlaylistIds(playlistIds)
			.stream()
			.collect(Collectors.groupingBy(pc -> pc.getPlaylist().getId()));

		List<UUID> contentIds = contentsMap.values().stream()
			.flatMap(List::stream)
			.map(pc -> pc.getContent().getId())
			.toList();

		Map<UUID, List<String>> tagsMap = contentIds.isEmpty()
			? Collections.emptyMap()
			: contentTagRepository.findByContentIds(contentIds)
			.stream()
			.collect(Collectors.groupingBy(
				ct -> ct.getContent().getId(),
				Collectors.mapping(ct -> ct.getTag().getName(), Collectors.toList())
			));

		return toDto(playlist, countMap, subscribedIds, contentsMap, tagsMap);
	}

	private ContentSummary toContentSummary(Content content, Map<UUID, List<String>> tagsMap) {
		List<String> tags = tagsMap.getOrDefault(content.getId(), Collections.emptyList());

		String fullThumbnailUrl = buildImageUrl(content.getType(), content.getThumbnailUrl());

		return ContentSummary.builder()
			.id(content.getId())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(fullThumbnailUrl)
			.tags(tags)
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.build();
	}

	private String buildImageUrl(Type type, String path) {
		if (path == null) {
			return null;
		}

		if (path.startsWith("http://") || path.startsWith("https://")) {
			return path;
		}

		return switch (type) {
			case MOVIE, TV_SERIES -> TMDB_IMAGE_BASE_URL + path;
			case SPORTS -> path;
		};
	}

	private String encodeCursor(UUID id, Instant updatedAt) {
		String raw = id.toString() + "|" + updatedAt.toString();
		return java.util.Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
}
