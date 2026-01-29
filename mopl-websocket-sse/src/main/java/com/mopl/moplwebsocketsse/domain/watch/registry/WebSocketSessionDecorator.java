package com.mopl.moplwebsocketsse.domain.watch.registry;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketSessionDecorator extends WebSocketHandlerDecorator {

	private final WebSocketSessionHolder holder;

	public WebSocketSessionDecorator(WebSocketHandler delegate, WebSocketSessionHolder holder) {
		super(delegate);
		this.holder = holder;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		holder.add(session.getId(), session);
		log.debug("[WebSocketSessionDecorator] Connection established. sessionId={}", session.getId());
		super.afterConnectionEstablished(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		holder.remove(session.getId());
		log.debug("[WebSocketSessionDecorator] Connection closed. sessionId={}, status={}",
			session.getId(), closeStatus);
		super.afterConnectionClosed(session, closeStatus);
	}
}