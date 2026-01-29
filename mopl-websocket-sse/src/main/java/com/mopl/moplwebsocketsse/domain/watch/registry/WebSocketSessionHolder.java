package com.mopl.moplwebsocketsse.domain.watch.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketSessionHolder {

	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	public void add(String sessionId, WebSocketSession session) {
		sessions.put(sessionId, session);
		log.debug("[WebSocketSessionHolder] Added. sessionId={}", sessionId);
	}

	public WebSocketSession get(String sessionId) {
		return sessions.get(sessionId);
	}

	public void remove(String sessionId) {
		WebSocketSession removed = sessions.remove(sessionId);
		if (removed != null) {
			log.debug("[WebSocketSessionHolder] Removed. sessionId={}", sessionId);
		}
	}

	public int size() {
		return sessions.size();
	}
}