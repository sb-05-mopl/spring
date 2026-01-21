package com.mopl.moplwebsocketsse.domain.notification.event;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class NotificationMessageBuilder {

	public record Message(String title, String content) {
	}

	public Message build(NotificationEvent event) {
		Map<String, String> meta = event.meta() == null ? Map.of() : event.meta();

		return switch (event.type()) {
			case ROLE_CHANGED -> {
				String oldRole = NotificationMetaSpec.get(meta, NotificationMetaSpec.OLD_ROLE, "알 수 없는 기존 권한");
				String newRole = NotificationMetaSpec.get(meta, NotificationMetaSpec.NEW_ROLE, "알 수 없는 새 권한");

				yield new Message("내 권한이 변경되었어요.",
					"내 권한이 " + oldRole + "에서 " + newRole + "(으)로 변경되었어요.");
			}

			case PLAYLIST_SUBSCRIBED -> {
				String playlistTitle = NotificationMetaSpec.get(meta, NotificationMetaSpec.PLAYLIST_TITLE, "알 수 없는 제목");
				String subscriberName = NotificationMetaSpec.get(meta, NotificationMetaSpec.SUBSCRIBER_NAME,
					"알 수 없는 사용자");

				yield new Message(
					"내 플레이리스트에 구독자가 추가되었어요.",
					playlistTitle + "을(를) " + subscriberName + "님이 구독했어요."
				);
			}

			case SUBSCRIBED_PLAYLIST_CONTENT_ADDED -> {
				String playlistTitle = NotificationMetaSpec.get(meta, NotificationMetaSpec.PLAYLIST_TITLE, "알 수 없는 제목");
				String contentTitle = NotificationMetaSpec.get(meta, NotificationMetaSpec.CONTENT_TITLE, "알 수 없는 제목");

				yield new Message(
					playlistTitle + " 콘텐츠 추가",
					contentTitle + "이(가) 새로 추가되었어요."
				);
			}

			case FOLLOWEE_ACTIVITY -> buildFolloweeActivity(meta);

			case FOLLOWED_BY_USER -> {
				String followerName = NotificationMetaSpec.get(meta, NotificationMetaSpec.FOLLOWER_NAME, "알 수 없는 사용자");

				yield new Message(
					"새로운 팔로워가 생겼어요.",
					followerName + "님이 나를 팔로우하기 시작했어요."
				);
			}

			case DIRECT_MESSAGE_RECEIVED -> {
				String senderName = NotificationMetaSpec.get(meta, NotificationMetaSpec.SENDER_NAME, "알 수 없는 사용자");
				String content = NotificationMetaSpec.get(meta, NotificationMetaSpec.DIRECT_MESSAGE_CONTENT,
					"알 수 없는 내용");

				yield new Message(
					"[DM] " + senderName,
					content
				);
			}
		};
	}

	private Message buildFolloweeActivity(Map<String, String> meta) {
		String actorName = NotificationMetaSpec.get(meta, NotificationMetaSpec.ACTOR_NAME, "알 수 없는 사용자");
		String kind = NotificationMetaSpec.get(meta, NotificationMetaSpec.ACTIVITY_KIND, "알 수 없는 종류의 활동");

		if (kind == null || kind.isBlank()) {
			// 정보가 없을 시 디폴트 메시지
			return new Message("팔로이 활동 감지", "팔로우한 사용자의 새로운 활동이 있습니다.");
		}

		return switch (kind) {
			case "PLAYLIST_CREATED" -> {
				String playlistTitle = NotificationMetaSpec.get(meta, NotificationMetaSpec.PLAYLIST_TITLE, "알 수 없는 제목");
				String playlistDescription = NotificationMetaSpec.get(meta, NotificationMetaSpec.PLAYLIST_DESCRIPTION,
					"알 수 없는 설명");

				yield new Message(
					actorName + "님이 플리를 만들었어요.",
					"[" + playlistTitle + "] " + playlistDescription
				);
			}

			case "REVIEW_CREATED" -> {
				String contentTitle = NotificationMetaSpec.get(meta, NotificationMetaSpec.CONTENT_TITLE, "알 수 없는 제목");
				String reviewContent = NotificationMetaSpec.get(meta, NotificationMetaSpec.REVIEW_CONTENT, "알 수 없는 내용");

				yield new Message(
					actorName + "님이 리뷰를 작성했어요.",
					"[" + contentTitle + "] " + reviewContent
				);
			}

			case "WATCH_STARTED" -> {
				String contentTitle = NotificationMetaSpec.get(meta, NotificationMetaSpec.CONTENT_TITLE, "알 수 없는 제목");

				yield new Message(
					actorName + "님이 시청을 시작했어요.",
					"[" + contentTitle + "] 을(를) 시청중이에요."
				);
			}

			default -> new Message("팔로이 활동 감지", "팔로우한 사용자의 새로운 활동이 있습니다.");
		};
	}
}
