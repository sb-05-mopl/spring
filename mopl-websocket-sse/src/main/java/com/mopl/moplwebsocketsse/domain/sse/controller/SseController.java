package com.mopl.moplwebsocketsse.domain.sse.controller;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mopl.moplwebsocketsse.domain.sse.service.SseEmitterRegistry;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SseController {

	private final SseEmitterRegistry registry;

	@GetMapping("/api/sse")
	public SseEmitter subscribe(Authentication authentication) {
		UUID userId = (UUID)authentication.getPrincipal();
		return registry.connect(userId);
	}
}
