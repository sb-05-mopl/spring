package com.mopl.moplcore.domain.follow.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.follow.repository.FollowRepository;
import com.mopl.moplcore.global.event.notification.NotificationEvent;
import com.mopl.moplcore.global.event.notification.NotificationEventFactory;
import com.mopl.moplcore.global.event.notification.NotificationMetaSpec;
import com.mopl.moplcore.global.event.publisher.notification.NotificationEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolloweeActivityNotifier {

	private final FollowRepository followRepository;
	private final NotificationEventPublisher notificationEventPublisher;

	@Transactional
	public void notifyFollowers(
		UUID actorID,
		FolloweeActivityKind activityKind,
		// 플레이리스트 생성 시 신규 플레이리스트의 아이디가 referenceId
		// 리뷰 생성 시 리뷰가 추가된 콘텐츠의 아이디가 referenceId
		// 시청 시작 시 대상 콘텐츠의 아이디가 referenceId
		UUID referenceId,
		Map<String, String> meta
	) {
		List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(actorID);

		if (followerIds == null || followerIds.isEmpty()) {
			return;
		}

		for (UUID receiverId : followerIds) {
			if (receiverId.equals(actorID)) {
				continue;
			}

			NotificationEvent baseEvent = NotificationEventFactory.followeeActivity(
				receiverId,
				actorID,
				activityKind.name(),
				referenceId
			);

			NotificationEvent addedEvent = addMeta(baseEvent, meta);

			NotificationMetaSpec.validate(addedEvent);
			notificationEventPublisher.publish(addedEvent);
		}
	}

	private NotificationEvent addMeta(NotificationEvent baseEvent, Map<String, String> meta) {
		if (meta == null || meta.isEmpty()) {
			return baseEvent;
		}

		Map<String, String> added = new HashMap<>(baseEvent.meta());
		added.putAll(meta);

		return new NotificationEvent(
			baseEvent.eventId(),
			baseEvent.createdAt(),
			baseEvent.receiverId(),
			baseEvent.level(),
			baseEvent.type(),
			added
		);
	}
}
