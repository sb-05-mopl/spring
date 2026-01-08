package com.mopl.moplwebsocketsse.domain.notification.controller;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplwebsocketsse.domain.notification.dto.CursorResponseNotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortBy;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortDirection;
import com.mopl.moplwebsocketsse.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public CursorResponseNotificationDto<NotificationDto> findNotifications(
		Authentication authentication,
		@RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly,
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "idAfter", required = false) UUID idAfter,
		@RequestParam(name = "limit", defaultValue = "20") int limit,
		@RequestParam(name = "sortBy", defaultValue = "CREATED_AT") NotificationSortBy sortBy,
		@RequestParam(name = "sortDirection", defaultValue = "ASCENDING") NotificationSortDirection sortDirection
	) {
		UUID userId = (UUID)authentication.getPrincipal();
		return notificationService.findNotifications(
			userId, unreadOnly, cursor, idAfter, limit, sortBy, sortDirection
		);
	}

	@PatchMapping("/{id}/read")
	public NotificationDto markAsRead(Authentication authentication, @PathVariable("id") UUID id) {
		UUID userId = (UUID)authentication.getPrincipal();
		return notificationService.markAsRead(userId, id);
	}
}
