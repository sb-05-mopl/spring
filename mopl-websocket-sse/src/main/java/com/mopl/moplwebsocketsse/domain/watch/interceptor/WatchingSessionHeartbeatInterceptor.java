package com.mopl.moplwebsocketsse.domain.watch.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.watch.registry.WatchingSessionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionHeartbeatInterceptor implements ChannelInterceptor {

	private final WatchingSessionRegistry registry;

	@Override
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		if (!sent) {
			return;
		}

		SimpMessageType type = SimpMessageHeaderAccessor.getMessageType(message.getHeaders());
		if (type != SimpMessageType.HEARTBEAT) {
			return;
		}

		String wsSessionId = SimpMessageHeaderAccessor.getSessionId(message.getHeaders());
		if (wsSessionId == null) {
			return;
		}

		registry.updateLastActiveTimeByWsSessionId(wsSessionId);

		log.trace("[Heartbeat] Updated lastActiveTime. wsId={}", wsSessionId);
	}
}