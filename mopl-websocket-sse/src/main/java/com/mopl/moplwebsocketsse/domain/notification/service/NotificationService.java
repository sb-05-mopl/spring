package com.mopl.moplwebsocketsse.domain.notification.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.notification.dto.CursorResponseNotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationDto;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortBy;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortDirection;
import com.mopl.moplwebsocketsse.domain.notification.entity.Notification;
import com.mopl.moplwebsocketsse.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	@Transactional(readOnly = true)
	public CursorResponseNotificationDto<NotificationDto> findNotifications(
		UUID me,
		boolean unreadOnly,
		String cursor,
		UUID idAfter,
		int limit,
		NotificationSortBy sortBy,
		NotificationSortDirection sortDirection
	) {
		if (sortBy != NotificationSortBy.CREATED_AT) {
			throw new IllegalArgumentException("지원되지 않는 정렬 방식입니다 : " + sortBy);
		}
		if (sortDirection == null) {
			throw new IllegalArgumentException("정렬 방향은 필수입니다.");
		}

		int size = Math.min(Math.max(limit, 1), 100);

		List<Notification> fetched = notificationRepository.findByReceiverIdWithCursor(
			me, unreadOnly, cursor, idAfter, size, sortBy, sortDirection
		);

		boolean hasNext = fetched.size() > size;
		List<Notification> page = hasNext ? fetched.subList(0, size) : fetched;

		List<NotificationDto> data = page.stream().map(NotificationDto::from).toList();

		String nextCursor = null;
		UUID nextIdAfter = null;
		if (hasNext && !page.isEmpty()) {
			Notification last = page.get(page.size() - 1);
			nextCursor = last.getCreatedAt().toString();
			nextIdAfter = last.getId();
		}

		long totalCount = unreadOnly
			? notificationRepository.countByReceiverIdAndReadAtIsNull(me)
			: notificationRepository.countByReceiverId(me);

		return new CursorResponseNotificationDto<>(
			data,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			sortBy,
			sortDirection
		);
	}

	@Transactional
	public NotificationDto markAsRead(UUID me, UUID notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new IllegalArgumentException("Notification not found : " + notificationId));

		if (!notification.getReceiverId().equals(me)) {
			throw new AccessDeniedException("You are not allowed to read this notification : " + notificationId);
		}

		notification.markAsRead();
		return NotificationDto.from(notification);
	}

	@Transactional
	public NotificationDto saveIfAbsentFromEvent(NotificationDto dto) {
		if (notificationRepository.existsById(dto.id())) {
			return dto;
		}

		Notification entity = new Notification(
			dto.receiverId(),
			dto.title(),
			dto.content(),
			dto.level()
		);
		Notification saved = notificationRepository.save(entity);

		return NotificationDto.from(saved);
	}
}
