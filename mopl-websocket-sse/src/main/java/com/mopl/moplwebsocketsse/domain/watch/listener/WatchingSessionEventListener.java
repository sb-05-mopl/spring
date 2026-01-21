package com.mopl.moplwebsocketsse.domain.watch.listener;

import java.util.List;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionChange;
import com.mopl.moplwebsocketsse.domain.watch.entity.WatchingSession;
import com.mopl.moplwebsocketsse.domain.watch.event.WatchingSessionEventPublisher;
import com.mopl.moplwebsocketsse.domain.watch.event.WatchingSessionStartedEvent;
import com.mopl.moplwebsocketsse.domain.watch.registry.SessionMapping;
import com.mopl.moplwebsocketsse.domain.watch.registry.WatchingSessionRegistry;
import com.mopl.moplwebsocketsse.domain.watch.repository.WatchingSessionRepository;
import com.mopl.moplwebsocketsse.domain.watch.service.WatchingSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {

	private final WatchingSessionRepository wsRepository;
	private final WatchingSessionRegistry wsRegistry;
	private final WatchingSessionService wsService;
	private final WatchingSessionEventPublisher wsPublisher;
	private final SimpMessagingTemplate messagingTemplate;

	@EventListener
	public void handleSessionSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

		String destination = accessor.getDestination();
		if (destination == null || !destination.startsWith("/sub/contents/") || !destination.endsWith("/watch")) {
			return;
		}

		UUID contentId = parseContentIdFromDestination(destination);
		if (contentId == null) {
			return;
		}

		UUID userId = extractUserId(accessor);
		if (userId == null) {
			return;
		}

		String wsSessionId = accessor.getSessionId();
		String subscriptionId = accessor.getSubscriptionId();

		if (wsSessionId == null || subscriptionId == null) {
			return;
		}

		WatchingSession existing = wsRepository.findSessionByWatcherId(userId);
		boolean sameContentAlreadyWatching = existing != null && contentId.equals(existing.getContentId());

		WatchingSession watchingSession = new WatchingSession(userId, contentId);

		wsRepository.save(watchingSession);
		wsRegistry.register(wsSessionId, subscriptionId, watchingSession.getId(), userId, contentId);

		log.debug(
			"[WatchingSessionEventListener] SUBSCRIBE After registry. wsId={}, subId={}, watchingId={}, userId={}, contentId={}",
			wsSessionId, subscriptionId, watchingSession.getId(), userId, contentId
		);

		try {
			WatchingSessionChange message = wsService.createJoinMessage(watchingSession);
			broadcastToWatchers(contentId, message);

			if (!sameContentAlreadyWatching) {
				String actorName = message.watchingSession().watcher().name();
				String contentTitle = message.watchingSession().content().title();

				WatchingSessionStartedEvent startedEvent = WatchingSessionStartedEvent.of(
					userId,
					actorName,
					contentId,
					contentTitle,
					watchingSession.getId()
				);

				wsPublisher.publish(startedEvent);
			}
		} catch (Exception e) {
			log.error("[WatchingSessionEventListener] Failed to broadcast JOIN. sessionId={}", watchingSession.getId(),
				e);
		}
	}

	@EventListener
	public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

		String wsSessionId = accessor.getSessionId();
		String subscriptionId = accessor.getSubscriptionId();

		if (wsSessionId == null || subscriptionId == null) {
			return;
		}

		SessionMapping mapping = wsRegistry.removeBySubscriptionId(wsSessionId, subscriptionId);

		if (mapping == null) {
			return;
		}

		try {
			WatchingSessionChange message = wsService.createLeaveMessage(
				mapping.watchingSessionId(),
				mapping.contentId()
			);
			broadcastToWatchers(mapping.contentId(), message);
		} catch (Exception e) {
			log.error("[WatchingSessionEventListener] Failed to broadcast LEAVE. sessionId={}",
				mapping.watchingSessionId(), e);
		}

		wsRepository.delete(mapping.watchingSessionId(), mapping.contentId(), mapping.userId());

		log.debug(
			"[WatchingSessionEventListener] UNSUBSCRIBE. wsId={}, subId={}, watchingId={}, userId={}, contentId={}",
			wsSessionId, subscriptionId, mapping.watchingSessionId(), mapping.userId(), mapping.contentId());
	}

	@EventListener
	public void handleSessionDisconnect(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

		String wsSessionId = accessor.getSessionId();
		if (wsSessionId == null) {
			return;
		}

		List<SessionMapping> mappings = wsRegistry.removeAllByWsSessionId(wsSessionId);

		if (mappings.isEmpty()) {
			log.debug("[WatchingSessionEventListener] No mappings found on DISCONNECT. wsId={}", wsSessionId);
			return;
		}

		for (SessionMapping mapping : mappings) {
			try {
				WatchingSessionChange message = wsService.createLeaveMessage(
					mapping.watchingSessionId(),
					mapping.contentId()
				);
				broadcastToWatchers(mapping.contentId(), message);
			} catch (Exception e) {
				log.error("[WatchingSessionEventListener] Failed to broadcast LEAVE. sessionId={}",
					mapping.watchingSessionId(), e);
			}

			wsRepository.delete(mapping.watchingSessionId(), mapping.contentId(), mapping.userId());

			log.debug(
				"[WatchingSessionEventListener] DISCONNECT. wsId={}, subId={}, watchingId={}, userId={}, contentId={}",
				wsSessionId, mapping.subscriptionId(), mapping.watchingSessionId(),
				mapping.userId(), mapping.contentId());
		}
	}

	private UUID parseContentIdFromDestination(String destination) {
		if (destination == null)
			return null;

		String prefix = "/sub/contents/";
		String suffix = "/watch";

		if (!destination.startsWith(prefix) || !destination.endsWith(suffix)) {
			return null;
		}

		String contentId = destination.substring(
			prefix.length(),
			destination.length() - suffix.length()
		);

		try {
			return UUID.fromString(contentId);
		} catch (IllegalArgumentException e) {
			log.debug("[WatchingSessionEventListener] Failed to parse contentId. destination={}", destination, e);
			return null;
		}
	}

	private UUID extractUserId(StompHeaderAccessor accessor) {
		Authentication auth = (Authentication)accessor.getUser();

		if (auth == null || auth.getPrincipal() == null) {
			return null;
		}

		return (UUID)auth.getPrincipal();
	}

	private void broadcastToWatchers(UUID contentId, WatchingSessionChange message) {
		String destination = "/sub/contents/" + contentId + "/watch";
		messagingTemplate.convertAndSend(destination, message);
		log.debug("[WatchingSessionEventListener] Broadcasted {} to {}. watcherCount={}",
			message.type(), destination, message.watcherCount());
	}
}