package com.mopl.moplwebsocketsse.domain.notification.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public List<NotificationDto> list(
		Authentication authentication,
		@RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly
	) {
		UUID userId = (UUID)authentication.getPrincipal();
		return notificationService.list(userId, unreadOnly);
	}

	@PatchMapping("/{id}/read")
	public NotificationDto markAsRead(Authentication authentication, @PathVariable("id") UUID id) {
		UUID userId = (UUID)authentication.getPrincipal();
		return notificationService.markAsRead(userId, id);
	}
}
