package com.mopl.moplwebsocketsse.domain.watch.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplwebsocketsse.domain.watch.dto.CursorResponseWatchingSessionDto;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionDto;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionSearchRequest;
import com.mopl.moplwebsocketsse.domain.watch.service.WatchingSessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WatchingSessionController {

	private final WatchingSessionService watchingSessionService;

	@GetMapping("/contents/{contentId}/watching-sessions")
	public ResponseEntity<CursorResponseWatchingSessionDto> findWatchingSessionsByContent(
		@PathVariable UUID contentId,
		@Valid WatchingSessionSearchRequest request
	) {
		log.debug("[WatchingSessionController] Find watching sessions by content. contentId={}, limit={}",
			contentId, request.limit());

		CursorResponseWatchingSessionDto response = watchingSessionService.findSessionsByContent(
			contentId,
			request
		);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/users/{watcherId}/watching-sessions")
	public ResponseEntity<WatchingSessionDto> findWatchingSessionsByWatcher(
		@PathVariable UUID watcherId
	) {
		log.debug("[WatchingSessionController] Find watching session by watcher. watcherId={}", watcherId);

		WatchingSessionDto session = watchingSessionService.findSessionByWatcher(watcherId);

		return ResponseEntity.ok(session);
	}
}
