package com.mopl.moplwebsocketsse.domain.sse.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mopl.moplwebsocketsse.domain.sse.service.SseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

	private final SseService sseService;

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(
		Authentication authentication,
		@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
	) {
		UUID userId = (UUID)authentication.getPrincipal();
		return sseService.connect(userId);
	}
}
