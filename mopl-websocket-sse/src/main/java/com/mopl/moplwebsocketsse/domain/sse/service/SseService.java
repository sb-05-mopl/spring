package com.mopl.moplwebsocketsse.domain.sse.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseService {

	private static final long TIMEOUT_MS = 60L * 60 * 1000; // 1시간

	private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public SseEmitter connect(UUID userId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
		emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

		emitter.onCompletion(() -> removeEmitter(userId, emitter));
		emitter.onTimeout(() -> removeEmitter(userId, emitter));
		emitter.onError((e) -> removeEmitter(userId, emitter));

		send(emitter, "connected", UUID.randomUUID().toString(),
			Map.of("connectedAt", Instant.now().toString()));

		return emitter;
	}

	public void broadcast(UUID userId, String eventName, String eventId, Object data) {
		List<SseEmitter> list = emitters.get(userId);

		if (list == null || list.isEmpty()) {
			return;
		}

		for (SseEmitter emitter : list) {
			try {
				emitter.send(SseEmitter.event()
					.name(eventName)
					.id(eventId)
					.data(data, MediaType.APPLICATION_JSON));
			} catch (IOException e) {
				removeEmitter(userId, emitter);
			}
		}
	}

	public void send(SseEmitter emitter, String eventName, String eventId, Object data) {
		try {
			emitter.send(SseEmitter.event()
				.name(eventName)
				.id(eventId)
				.data(data, MediaType.APPLICATION_JSON));
		} catch (IOException e) {
			try {
				emitter.complete();
			} catch (Exception ignore) {
			}
		}
	}

	private void removeEmitter(UUID userId, SseEmitter emitter) {
		List<SseEmitter> list = emitters.get(userId);

		if (list == null || list.isEmpty()) {
			return;
		}

		list.remove(emitter);
		if (list.isEmpty()) {
			emitters.remove(userId);
		}
	}
}
