package com.mopl.moplwebsocketsse.domain.notification.event;

import com.mopl.moplwebsocketsse.domain.notification.entity.NotificationLevel;

// 알림의 eventType을 enum으로 분류
public enum NotificationEventType {
	ROLE_CHANGED, // 나의 권한 변경됨
	PLAYLIST_SUBSCRIBED, // 나의 플레이리스트를 다른 유저가 구독
	SUBSCRIBED_PLAYLIST_CONTENT_ADDED, // 구독 중인 플레이리스트에 콘텐츠가 추가됨
	FOLLOWEE_ACTIVITY, // 팔로우한 유저의 주요 활동
	FOLLOWED_BY_USER, // 다른 유저가 나를 팔로우
	DIRECT_MESSAGE_RECEIVED; // DM 수신

	// 알림을 분류하여 level을 설정
	public NotificationLevel defaultLevel() {
		return switch (this) {
			case ROLE_CHANGED -> NotificationLevel.WARNING;
			case PLAYLIST_SUBSCRIBED -> NotificationLevel.INFO;
			case SUBSCRIBED_PLAYLIST_CONTENT_ADDED -> NotificationLevel.INFO;
			case FOLLOWEE_ACTIVITY -> NotificationLevel.INFO;
			case FOLLOWED_BY_USER -> NotificationLevel.INFO;
			case DIRECT_MESSAGE_RECEIVED -> NotificationLevel.INFO;
		};
	}
}
