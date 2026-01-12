package com.mopl.moplwebsocketsse.domain.notification.event;

public enum NotificationEventType {
	ROLE_CHANGED, // 나의 권한 변경됨
	PLAYLIST_SUBSCRIBED, // 나의 플레이리스트를 다른 유저가 구독
	SUBSCRIBED_PLAYLIST_CONTENT_ADDED, // 구독 중인 플레이리스트에 콘텐츠가 추가됨
	FOLLOWING_USER_ACTIVITY, // 팔로우한 유저의 주요 활동
	FOLLOWED_BY_USER, // 다른 유저가 나를 팔로우
	DIRECT_MESSAGE_RECEIVED // DM 수신
}
