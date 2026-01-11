package com.mopl.moplwebsocketsse.domain.watch.interceptor;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.watch.registry.SessionMapping;
import com.mopl.moplwebsocketsse.domain.watch.registry.WatchingSessionRegistry;
import com.mopl.moplwebsocketsse.domain.watch.repository.WatchingSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionHeartbeatInterceptor implements ChannelInterceptor {

	private final WatchingSessionRegistry registry;
	private final WatchingSessionRepository repository;

	@Override
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		if (!sent)
			return;

		SimpMessageType type = SimpMessageHeaderAccessor.getMessageType(message.getHeaders());
		if (type != SimpMessageType.HEARTBEAT)
			return;

		String wsSessionId = SimpMessageHeaderAccessor.getSessionId(message.getHeaders());
		if (wsSessionId == null)
			return;

		List<SessionMapping> mappings = registry.getAllByWsSessionId(wsSessionId);

		if (mappings.isEmpty()) {
			log.warn("[WatchingSessionHeartbeatInterceptor] No mappings found! wsId={}", wsSessionId);
			return;
		}

		for (SessionMapping mapping : mappings) {
			try {
				boolean alive = repository.refreshSessionTtl(mapping.watchingSessionId(), mapping.userId());

				if (!alive) {
					log.warn("[WatchingSessionHeartbeatInterceptor] Session not alive, removing. wsId={}, subId={}, watchingId={}",
						wsSessionId, mapping.subscriptionId(), mapping.watchingSessionId());
					registry.removeBySubscriptionId(mapping.webSocketSessionId(), mapping.subscriptionId());
				}
			} catch (Exception e) {
				log.warn("[WatchingSessionHeartbeatInterceptor] Redis 세션 갱신 실패. wsId={}, subId={}",
					wsSessionId, mapping.subscriptionId(), e);
			}
		}
	}
}