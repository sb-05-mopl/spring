package com.mopl.moplwebsocketsse.domain.watch.dto;

public record WatchingSessionChange(
	ChangeType type,
	WatchingSessionDto watchingSession,
	Long watcherCount
) {
	public enum ChangeType {
		JOIN,
		LEAVE
	}
}
