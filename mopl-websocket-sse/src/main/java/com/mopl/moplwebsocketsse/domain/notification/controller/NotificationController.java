package com.mopl.moplwebsocketsse.domain.notification.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplwebsocketsse.domain.notification.dto.CursorResponseNotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortBy;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortDirection;
import com.mopl.moplwebsocketsse.domain.notification.service.NotificationService;
import com.mopl.moplwebsocketsse.security.principal.MoplUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public CursorResponseNotificationDto<NotificationDto> findNotifications(
		@AuthenticationPrincipal MoplUserDetails moplUserDetails,
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "idAfter", required = false) UUID idAfter,
		@RequestParam(name = "limit", defaultValue = "20") int limit,
		@RequestParam(name = "sortBy", defaultValue = "createdAt") NotificationSortBy sortBy,
		@RequestParam(name = "sortDirection", defaultValue = "ASCENDING") NotificationSortDirection sortDirection
	) {
		UUID userId = moplUserDetails.getUserDto().getId();

		return notificationService.findNotifications(
			userId, cursor, idAfter, limit, sortBy, sortDirection
		);
	}
	/*
	@DeleteMapping("/{notificationId}")
	public void deleteNotification(
		@AuthenticationPrincipal MoplUserDetails moplUserDetails,
		@PathVariable("notificationId") UUID id
	) {
		UUID userId = moplUserDetails.getUserDto().getId();
		notificationService.deleteNotification(userId, id);
	} */
}
