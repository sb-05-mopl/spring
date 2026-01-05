package com.mopl.moplwebsocketsse.domain.sse.service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRegistry {

	private static final long TIMEOUT_MS = 60L * 60 * 1000; // 1시간

	private final ConcurrentHashMap<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter connect(UUID userId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
		emitters.put(userId, emitter);

		emitter.onCompletion(() -> emitters.remove(userId));
		emitter.onTimeout(() -> emitters.remove(userId));
		emitter.onError((e) -> emitters.remove(userId));

		send(userId, "connected", userId.toString(),
			Map.of("connectedAt", Instant.now().toString()));
		return emitter;
	}

	public void send(UUID userId, String eventName, String eventId, Object data) {
		org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = emitters.get(userId);

		if (emitter == null) {
			return;
		}

		try {
			emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
				.name(eventName)
				.id(eventId)
				.data(data, MediaType.APPLICATION_JSON));
		} catch (IOException e) {
			emitters.remove(userId);
		}
	}
}
