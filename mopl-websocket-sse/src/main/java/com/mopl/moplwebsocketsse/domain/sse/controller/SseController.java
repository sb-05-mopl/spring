package com.mopl.moplwebsocketsse.domain.sse.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.service.NotificationService;
import com.mopl.moplwebsocketsse.domain.sse.service.SseService;
import com.mopl.moplwebsocketsse.security.principal.MoplUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

	private final SseService sseService;
	private final NotificationService notificationService;

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(
		@AuthenticationPrincipal MoplUserDetails userDetails,
		@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
	) {
		UUID userId = userDetails.getUserDto().getId();

		SseEmitter emitter = sseService.connect(userId);

		List<NotificationDto> missed =
			notificationService.findMissedForReplay(userId, lastEventId, 100);

		for (NotificationDto notificationDto : missed) {
			sseService.send(
				emitter,
				"notifications",
				notificationDto.id().toString(),
				notificationDto
			);
		}
		return emitter;
	}
}
