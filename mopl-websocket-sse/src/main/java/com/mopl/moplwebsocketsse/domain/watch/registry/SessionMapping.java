package com.mopl.moplwebsocketsse.domain.watch.registry;

import java.util.UUID;

/**
 * 세션 매핑 정보
 *
 * @param webSocketSessionId WebSocket STOMP Session ID
 * @param watchingSessionId WatchingSession ID
 * @param userId 사용자 ID
 * @param contentId 콘텐츠 ID
 */
public record SessionMapping(
	String webSocketSessionId,
	String subscriptionId,
	UUID watchingSessionId,
	UUID userId,
	UUID contentId
) {}