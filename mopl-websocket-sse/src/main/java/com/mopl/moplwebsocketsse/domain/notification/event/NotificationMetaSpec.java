package com.mopl.moplwebsocketsse.domain.notification.event;

import java.util.List;
import java.util.Map;

public final class NotificationMetaSpec {
	private NotificationMetaSpec() {
	}

	// Meta key 상수
	public static final String OLD_ROLE = "oldRole"; // 기존 권한
	public static final String NEW_ROLE = "newRole"; // 새 권한
	public static final String CHANGED_BY = "changedBy"; // 권한 변경 주체

	public static final String PLAYLIST_ID = "playlistId"; // 알림이 발생한 플리의 아이디
	public static final String SUBSCRIBER_ID = "subscriberId"; // 내 플리를 구독한 유저의 아이디
	public static final String SUBSCRIBER_NAME = "subscriberName"; // 내 플리를 구독한 유저의 이름

	public static final String CONTENT_ID = "contentId"; // 내가 구독한 플리에 추가된 콘텐츠 아이디

	public static final String ACTOR_ID = "actorId"; // 활동 알림이 발생한 유저 아이디
	public static final String ACTOR_NAME = "actorName"; // 활동 알림이 발생한 유저 이름
	public static final String ACTIVITY_KIND = "activityKind"; // 해당 활동의 종류
	public static final String REFERENCE_ID = "referenceId"; // 해당 활동의 아이디

	public static final String REVIEW_ID = "reviewId"; // 활동 알림이 발생한 신규 리뷰 아이디
	public static final String REVIEW_CONTENT = "reviewContent"; // 활동 알림이 발생한 신규 리뷰 내용
	public static final String WATCHING_SESSION_ID = "watchingSessionId"; // 활동 알림이 발생한 시청 세션 아이디

	public static final String PLAYLIST_TITLE = "playlistTitle"; // 플레이리스트 제목
	public static final String PLAYLIST_DESCRIPTION = "playlistDescription"; // 신규 플레이리스트 설명
	public static final String CONTENT_TITLE = "contentTitle"; // 추가된 콘텐츠 제목

	public static final String FOLLOWER_ID = "followerId"; // 나를 팔로우한 사람의 아이디
	public static final String FOLLOWER_NAME = "followerName"; // 나를 팔로우한 사람의 이름

	public static final String CONVERSATION_ID = "conversationId"; // DM이 온 대화방의 아이디
	public static final String DIRECT_MESSAGE_ID = "directMessageId"; // 알림 발생 트리거 DM의 아이디
	public static final String DIRECT_MESSAGE_CONTENT = "directMessageContent"; // 알림 발생 트리거 DM의 내용
	public static final String SENDER_ID = "senderId"; // DM 발신자의 아이디
	public static final String SENDER_NAME = "senderName"; // DM 발신자의 이름

	public static final Map<NotificationEventType, List<String>> REQUIRED_KEYS = Map.of(
		NotificationEventType.ROLE_CHANGED,
		List.of(OLD_ROLE, NEW_ROLE, CHANGED_BY),

		NotificationEventType.PLAYLIST_SUBSCRIBED,
		List.of(PLAYLIST_ID, SUBSCRIBER_ID),

		NotificationEventType.SUBSCRIBED_PLAYLIST_CONTENT_ADDED,
		List.of(PLAYLIST_ID, CONTENT_ID),

		NotificationEventType.FOLLOWEE_ACTIVITY,
		List.of(ACTOR_ID, ACTIVITY_KIND, REFERENCE_ID),

		NotificationEventType.FOLLOWED_BY_USER,
		List.of(FOLLOWER_ID, FOLLOWER_NAME),

		NotificationEventType.DIRECT_MESSAGE_RECEIVED,
		List.of(CONVERSATION_ID, DIRECT_MESSAGE_ID, SENDER_ID)
	);

	public static List<String> requiredKeysOf(NotificationEventType type) {
		return REQUIRED_KEYS.getOrDefault(type, List.of());
	}

	public static void validate(NotificationEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("이벤트가 존재하지 않습니다.");
		}

		if (event.type() == null) {
			throw new IllegalArgumentException("이벤트 타입이 존재하지 않습니다.");
		}

		Map<String, String> meta = event.meta();
		if (meta == null) {
			meta = Map.of();
		}

		for (String key : requiredKeysOf(event.type())) {
			String metaValue = meta.get(key);
			if (metaValue == null || metaValue.isBlank()) {
				throw new IllegalArgumentException("Meta 키 없음 : " + key + " (type=" + event.type() + ")");
			}
		}
	}

	public static String get(Map<String, String> meta, String key, String fallback) {
		if (meta == null) {
			return fallback;
		}
		String value = meta.get(key);
		return (value == null || value.isBlank()) ? fallback : value;
	}
}
