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
import com.mopl.moplwebsocketsse.domain.notification.event.NotificationEvent;
import com.mopl.moplwebsocketsse.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public record NotificationReplay(UUID eventId, NotificationDto payload) {
	}

	@Transactional(readOnly = true)
	public CursorResponseNotificationDto<NotificationDto> findNotifications(
		UUID me,
		String cursor,
		UUID idAfter,
		int limit,
		NotificationSortBy sortBy,
		NotificationSortDirection sortDirection
	) {
		if (sortBy != NotificationSortBy.createdAt) {
			throw new IllegalArgumentException("지원되지 않는 정렬 방식입니다 : " + sortBy);
		}
		if (sortDirection == null) {
			throw new IllegalArgumentException("정렬 방향은 필수입니다.");
		}

		int size = Math.min(Math.max(limit, 1), 100);

		List<Notification> fetched = notificationRepository.findByReceiverIdWithCursor(
			me, cursor, idAfter, size, sortBy, sortDirection
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

		long totalCount = notificationRepository.countByReceiverId(me);

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
	public void deleteNotification(UUID me, UUID notificationId) {
		if (me == null) {
			throw new AccessDeniedException("사용자 ID가 존재하지 않아 접근이 거부되었습니다.");
		}

		long deleted = notificationRepository.deleteByIdAndReceiverId(notificationId, me);

		if (deleted == 0) {
			throw new IllegalArgumentException("Notification not found : " + notificationId);
		}
	}

	@Transactional(readOnly = true)
	public List<NotificationReplay> findMissedForReplay(UUID receiverId, String lastEventId, int limit) {
		if (lastEventId == null || lastEventId.isBlank()) {
			return List.of();
		}

		UUID pivotEventId;
		try {
			pivotEventId = UUID.fromString(lastEventId.trim());
		} catch (Exception e) {
			return List.of();
		}

		Notification pivot = notificationRepository
			.findByEventIdAndReceiverId(pivotEventId, receiverId).orElse(null);
		if (pivot == null) {
			return List.of();
		}

		int size = Math.min(Math.max(limit, 1), 100);

		List<Notification> fetched = notificationRepository.findByReceiverIdWithCursor(
			receiverId,
			pivot.getCreatedAt().toString(),
			pivot.getId(),
			size,
			NotificationSortBy.createdAt,
			NotificationSortDirection.ASCENDING
		);

		if (fetched.size() > size) {
			fetched = fetched.subList(0, size);
		}

		return fetched.stream()
			.map(n -> new NotificationReplay(n.getEventId(), NotificationDto.from(n)))
			.toList();
	}

	@Transactional
	public NotificationDto createFromEvent(NotificationEvent event, String title, String content) {
		Notification existed = notificationRepository
			.findByEventIdAndReceiverId(event.eventId(), event.receiverId())
			.orElse(null);
		if (existed != null) {
			return NotificationDto.from(existed);
		}

		Notification notification = new Notification(
			event.receiverId(),
			title,
			content,
			event.level(),
			event.eventId()
		);

		return NotificationDto.from(notificationRepository.save(notification));
	}
}
