package com.mopl.moplcore.global.event.notification;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mopl.moplcore.domain.user.entity.User;

public class NotificationEventFactory {

	private NotificationEventFactory() {
	}

	// 권한 변경 시
	public static NotificationEvent roleChanged(
		UUID receiverId,
		String oldRole,
		String newRole,
		UUID changedBy
	) {
		return eventTemplate(
			receiverId,
			NotificationEventType.ROLE_CHANGED,
			Map.of(
				NotificationMetaSpec.OLD_ROLE, oldRole,
				NotificationMetaSpec.NEW_ROLE, newRole,
				NotificationMetaSpec.CHANGED_BY, changedBy.toString()
			)
		);
	}

	// 내 플리에 구독 발생 시
	public static NotificationEvent playlistSubscribed(
		UUID receiverId,
		UUID playlistId,
		UUID subscriberId,
		String playlistTitle,
		String subscriberName
	) {
		Map<String, String> meta = new HashMap<>();
		meta.put(NotificationMetaSpec.PLAYLIST_ID, playlistId.toString());
		meta.put(NotificationMetaSpec.SUBSCRIBER_ID, subscriberId.toString());

		if (playlistTitle != null && !playlistTitle.isBlank()) {
			meta.put(NotificationMetaSpec.PLAYLIST_TITLE, playlistTitle);
		}

		if (subscriberName != null && !subscriberName.isBlank()) {
			meta.put(NotificationMetaSpec.SUBSCRIBER_NAME, subscriberName);
		}

		return eventTemplate(
			receiverId,
			NotificationEventType.PLAYLIST_SUBSCRIBED,
			meta
		);
	}

	// 구독 중인 플리에 콘텐츠 추가 시
	public static NotificationEvent subscribedPlaylistContentAdded(
		UUID receiverId,
		UUID playlistId,
		UUID contentId,
		String playlistTitle,
		String contentTitle
	) {
		Map<String, String> meta = new HashMap<>();
		meta.put(NotificationMetaSpec.PLAYLIST_ID, playlistId.toString());
		meta.put(NotificationMetaSpec.CONTENT_ID, contentId.toString());

		if (playlistTitle != null && !playlistTitle.isBlank()) {
			meta.put(NotificationMetaSpec.PLAYLIST_TITLE, playlistTitle);
		}

		if (contentTitle != null && !contentTitle.isBlank()) {
			meta.put(NotificationMetaSpec.CONTENT_TITLE, contentTitle);
		}

		return eventTemplate(
			receiverId,
			NotificationEventType.SUBSCRIBED_PLAYLIST_CONTENT_ADDED,
			meta
		);
	}

	// 팔로우한 사용자의 주요 활동 발생 시
	public static NotificationEvent followeeActivity(
		UUID receiverId,
		UUID actorId,
		String activityKind,
		UUID referenceId
	) {
		return eventTemplate(
			receiverId,
			NotificationEventType.FOLLOWEE_ACTIVITY,
			Map.of(
				NotificationMetaSpec.ACTOR_ID, actorId.toString(),
				NotificationMetaSpec.ACTIVITY_KIND, activityKind,
				NotificationMetaSpec.REFERENCE_ID, referenceId.toString()
			)
		);
	}

	// 다른 유저가 나를 팔로우
	public static NotificationEvent followedByUser(
		UUID receiverId,
		UUID followerId,
		String followerName
	) {
		Map<String, String> meta = new HashMap<>();
		meta.put(NotificationMetaSpec.FOLLOWER_ID, followerId.toString());

		if (followerName != null && !followerName.isBlank()) {
			meta.put(NotificationMetaSpec.FOLLOWER_NAME, followerName);
		}

		return eventTemplate(
			receiverId,
			NotificationEventType.FOLLOWED_BY_USER,
			meta
		);
	}

	// DM 수신
	public static NotificationEvent directMessageReceived(
		UUID receiverId,
		UUID conversationId,
		UUID directMessageId,
		User sender,
		String content
	) {
		return new NotificationEvent(
			directMessageId,
			Instant.now(),
			receiverId,
			NotificationEventType.DIRECT_MESSAGE_RECEIVED.defaultLevel(),
			NotificationEventType.DIRECT_MESSAGE_RECEIVED,
			Map.of(
				NotificationMetaSpec.CONVERSATION_ID, conversationId.toString(),
				NotificationMetaSpec.DIRECT_MESSAGE_ID, directMessageId.toString(),
				NotificationMetaSpec.SENDER_ID, sender.getId().toString(),
				NotificationMetaSpec.SENDER_NAME, sender.getName(),
				NotificationMetaSpec.DIRECT_MESSAGE_CONTENT, content
			)
		);
	}

	private static NotificationEvent eventTemplate(
		UUID receiverId,
		NotificationEventType type,
		Map<String, String> meta
	) {
		return new NotificationEvent(
			UUID.randomUUID(),
			Instant.now(),
			receiverId,
			type.defaultLevel(),
			type,
			meta
		);
	}
}
