package com.mopl.moplwebsocketsse.domain.watch.registry;

import java.time.Instant;
import java.util.UUID;

import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;

/**
 * 세션 매핑 정보
 */
@Getter
public class SessionMapping {

	private final String webSocketSessionId;
	private final String subscriptionId;
	private final UUID watchingSessionId;
	private final UUID userId;
	private final UUID contentId;
	private final WebSocketSession webSocketSession;

	private volatile Instant lastActiveTime;

	/**
	 * @param webSocketSessionId 웹소켓 세션 아이디
	 * @param subscriptionId 구독 아이디
	 * @param watchingSessionId 시청 세션 UUID
	 * @param userId 사용자 UUID
	 * @param contentId 콘텐츠 UUID
	 * @param webSocketSession 인스턴스
	 */
	public SessionMapping(
		String webSocketSessionId,
		String subscriptionId,
		UUID watchingSessionId,
		UUID userId,
		UUID contentId,
		WebSocketSession webSocketSession
	) {
		this.webSocketSessionId = webSocketSessionId;
		this.subscriptionId = subscriptionId;
		this.watchingSessionId = watchingSessionId;
		this.userId = userId;
		this.contentId = contentId;
		this.webSocketSession = webSocketSession;
		this.lastActiveTime = Instant.now();
	}

	public void updateLastActiveTime() {
		this.lastActiveTime = Instant.now();
	}
}