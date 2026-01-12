package com.mopl.moplwebsocketsse.domain.notification.event;

import java.util.List;
import java.util.Map;

// NotificationEvent.meta 키
public class NotificationMetaSpec {
	private NotificationMetaSpec() {
	}

	public static final Map<NotificationEventType, List<String>> REQUIRED_KEYS = Map.of(
		NotificationEventType.ROLE_CHANGED,
		List.of("oldRole", "newRole", "changedBy"), // 기존 권한, 새 권한, 변경한 사람

		NotificationEventType.PLAYLIST_SUBSCRIBED,
		List.of("playlistId", "subscriberId"), // 구독이 발생한 플리 아이디, 해당 구독자 아이디

		NotificationEventType.SUBSCRIBED_PLAYLIST_CONTENT_ADDED,
		List.of("playlistId", "contentId"), // 구독한 플리 아이디, 추가된 콘텐츠 아이디

		NotificationEventType.FOLLOWING_USER_ACTIVITY,
		List.of("actorId", "activityKind"), // 팔로우 대상자 아이디, 활동 종류

		NotificationEventType.FOLLOWED_BY_USER,
		List.of("followerId"), // 팔로워의 아이디

		NotificationEventType.DIRECT_MESSAGE_RECEIVED,
		List.of("conversationId", "directMessageId", "senderId") // DM의 대화방 아이디, DM 메시지 아이디, 발신자 이이디
	);
}
