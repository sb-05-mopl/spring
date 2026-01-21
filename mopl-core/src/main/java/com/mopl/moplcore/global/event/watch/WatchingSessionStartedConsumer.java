package com.mopl.moplcore.global.event.watch;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplcore.domain.follow.activity.FolloweeActivityKind;
import com.mopl.moplcore.domain.follow.activity.FolloweeActivityNotifier;
import com.mopl.moplcore.global.event.notification.NotificationMetaSpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionStartedConsumer {

	private final ObjectMapper objectMapper;
	private final FolloweeActivityNotifier followeeActivityNotifier;

	@KafkaListener(
		topics = "${mopl.kafka.topics.watching-session-started}",
		groupId = "mopl-core",
		containerFactory = "kafkaListenerContainerFactory"
	)
	public void onMessage(String message, Acknowledgment ack) throws Exception {
		try {
			WatchingSessionStartedEvent event =
				objectMapper.readValue(message, WatchingSessionStartedEvent.class);

			String actorName = (event.actorName() == null || event.actorName().isBlank())
				? "알 수 없는 이름"
				: event.actorName();

			String contentTitle = (event.contentTitle() == null || event.contentTitle().isBlank())
				? "알 수 없는 콘텐츠"
				: event.contentTitle();

			if (event.watchingSessionId() == null) {
				throw new IllegalArgumentException("watchingSessionId is null. payload=" + message);
			}

			Map<String, String> meta = Map.of(
				NotificationMetaSpec.ACTOR_NAME, actorName,
				NotificationMetaSpec.CONTENT_TITLE, contentTitle,
				NotificationMetaSpec.WATCHING_SESSION_ID, event.watchingSessionId().toString()
			);

			followeeActivityNotifier.notifyFollowers(
				event.actorId(),
				FolloweeActivityKind.WATCH_STARTED,
				event.contentId(),
				meta
			);

			ack.acknowledge();
		} catch (Exception e) {
			log.error("[WatchingSessionStartedConsumer] failed. message={}", message, e);
			throw e;
		}
	}
}
