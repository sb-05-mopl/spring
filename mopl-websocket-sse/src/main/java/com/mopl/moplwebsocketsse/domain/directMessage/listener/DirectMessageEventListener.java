package com.mopl.moplwebsocketsse.domain.directMessage.listener;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.mopl.moplwebsocketsse.domain.directMessage.registry.DirectMessageSubscriptionRegistry;
import com.mopl.moplwebsocketsse.domain.directMessage.service.DirectMessageReadService;
import com.mopl.moplwebsocketsse.security.principal.MoplUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectMessageEventListener {

	private static final String DM_DEST_PREFIX = "/sub/conversations/";
	private static final String DM_DEST_SUFFIX = "/direct-messages";

	private final DirectMessageReadService directMessageReadService;

	private record Subscription(UUID conversationId, UUID userId) {
	}

	private final DirectMessageSubscriptionRegistry registry;
	private final Map<String, Subscription> subscriptionMap = new ConcurrentHashMap<>();

	@EventListener
	public void onSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

		String destination = accessor.getDestination();
		UUID userId = extractUserId(accessor.getUser());
		if (destination == null || userId == null) {
			return;
		}

		UUID conversationId = parseConversationId(destination);
		if (conversationId == null) {
			return;
		}

		String sessionId = accessor.getSessionId();
		String subscriptionId = accessor.getSubscriptionId();
		if (sessionId == null || subscriptionId == null) {
			return;
		}

		registry.subscribe(conversationId, userId);
		subscriptionMap.put(key(sessionId, subscriptionId), new Subscription(conversationId, userId));

		directMessageReadService.clearDmNotifications(userId, conversationId);

		log.debug("[DM][SUB] userId={}, conversationId={}, sessionId={}, subId={}",
			userId, conversationId, sessionId, subscriptionId);
	}

	@EventListener
	public void onUnsubscribe(SessionUnsubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

		String sessionId = accessor.getSessionId();
		String subscriptionId = accessor.getSubscriptionId();
		if (sessionId == null || subscriptionId == null) {
			return;
		}

		Subscription sub = subscriptionMap.remove(key(sessionId, subscriptionId));
		if (sub == null) {
			return;
		}

		registry.unsubscribe(sub.conversationId(), sub.userId());

		log.debug("[DM][UNSUB] userId={}, conversationId={}, sessionId={}, subId={}",
			sub.userId(), sub.conversationId(), sessionId, subscriptionId);
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();
		if (sessionId == null) {
			return;
		}

		String prefix = sessionId + ":";
		subscriptionMap.entrySet().removeIf(entry -> {
			if (!entry.getKey().startsWith(prefix)) {
				return false;
			}
			Subscription sub = entry.getValue();
			registry.unsubscribe(sub.conversationId(), sub.userId());

			log.debug("[DM][DISCONNECT-CLEAN] userId={}, conversationId={}, sessionId={}",
				sub.userId(), sub.conversationId(), sessionId);
			return true;
		});
	}

	private UUID parseConversationId(String destination) {
		if (!destination.startsWith(DM_DEST_PREFIX) || !destination.endsWith(DM_DEST_SUFFIX)) {
			return null;
		}

		String between = destination.substring(
			DM_DEST_PREFIX.length(),
			destination.length() - DM_DEST_SUFFIX.length()
		);

		try {
			return UUID.fromString(between);
		} catch (Exception e) {
			return null;
		}
	}

	private UUID extractUserId(Principal principal) {
		if (principal == null) {
			return null;
		}

		if (principal instanceof Authentication auth) {
			Object p = auth.getPrincipal();

			if (p instanceof MoplUserDetails mud && mud.getUserDto() != null) {
				return mud.getUserDto().getId();
			}

			if (p instanceof UUID uuid) {
				return uuid;
			}

			if (p instanceof String s) {
				try {
					return UUID.fromString(s);
				} catch (Exception ignored) {
				}
			}
		}

		if (principal instanceof Principal p) {
			try {
				return UUID.fromString(p.getName());
			} catch (Exception ignored) {
			}
		}

		return null;
	}

	private String key(String sessionId, String subscriptionId) {
		return sessionId + ":" + subscriptionId;
	}
}
