package com.mopl.moplwebsocketsse.domain.notification.message;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.notification.event.NotificationEvent;
import com.mopl.moplwebsocketsse.domain.notification.event.NotificationMetaSpec;

@Component
public class NotificationMessageBuilder {

	public record Message(String title, String content) {
	}

	public Message build(NotificationEvent event) {
		Map<String, String> meta = event.meta() == null ? Map.of() : event.meta();

		return switch (event.type()) {
			case ROLE_CHANGED -> {
				String oldRole = NotificationMetaSpec.get(meta, NotificationMetaSpec.OLD_ROLE);
				String newRole = NotificationMetaSpec.get(meta, NotificationMetaSpec.NEW_ROLE);
				yield new Message("권한 변경", oldRole + " -> " + newRole);
			}

			case PLAYLIST_SUBSCRIBED -> new Message(
				"플레이리스트 구독", "내 플레이리스트에 구독자가 추가되었습니다."
			);

			case SUBSCRIBED_PLAYLIST_CONTENT_ADDED -> new Message(
				"콘텐츠 추가", "구독 중인 플레이리스트에 콘텐츠가 추가되었습니다."
			);

			case FOLLOWING_USER_ACTIVITY -> new Message(
				"팔로이 활동 감지", "팔로우한 사용자의 새로운 활동이 있습니다."
			);

			case FOLLOWED_BY_USER -> new Message(
				"새로운 팔로워", "누군가 나를 팔로우했습니다."
			);

			case DIRECT_MESSAGE_RECEIVED -> new Message(
				"DM 수신", "메시지가 도착했습니다."
			);
		};
	}
}
