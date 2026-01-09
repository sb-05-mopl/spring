package com.mopl.moplwebsocketsse.domain.notification.repository;

import static com.mopl.moplwebsocketsse.domain.notification.entity.QNotification.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortBy;
import com.mopl.moplwebsocketsse.domain.notification.dto.NotificationSortDirection;
import com.mopl.moplwebsocketsse.domain.notification.entity.Notification;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Notification> findByReceiverIdWithCursor(
		UUID receiverId,
		String cursor,
		UUID idAfter,
		int limit,
		NotificationSortBy sortBy,
		NotificationSortDirection sortDirection
	) {
		return queryFactory
			.selectFrom(notification)
			.where(
				receiverIdEq(receiverId),
				cursorCondition(cursor, idAfter, sortBy, sortDirection)
			)
			.orderBy(orderSpecifier(sortBy, sortDirection))
			.limit((long)limit + 1)
			.fetch();
	}

	private BooleanExpression receiverIdEq(UUID receiverId) {
		if (receiverId == null) {
			throw new IllegalArgumentException("receiverId is null");
		}
		return notification.receiverId.eq(receiverId);
	}

	private BooleanExpression cursorCondition(
		String cursor,
		UUID idAfter,
		NotificationSortBy sortBy,
		NotificationSortDirection sortDirection
	) {
		if (cursor == null || cursor.isBlank() || idAfter == null) {
			return null;
		}

		Instant cursorTime = Instant.parse(cursor);
		boolean isDesc = (sortDirection == NotificationSortDirection.DESCENDING);

		return switch (sortBy) {
			case createdAt -> isDesc
				? notification.createdAt.lt(cursorTime)
				.or(notification.createdAt.eq(cursorTime).and(notification.id.lt(idAfter)))
				: notification.createdAt.gt(cursorTime)
				.or(notification.createdAt.eq(cursorTime).and(notification.id.gt(idAfter)));
		};
	}

	private OrderSpecifier<?>[] orderSpecifier(
		NotificationSortBy sortBy,
		NotificationSortDirection sortDirection
	) {
		boolean isDesc = (sortDirection == NotificationSortDirection.DESCENDING);

		return switch (sortBy) {
			case createdAt -> new OrderSpecifier[] {
				isDesc ? notification.createdAt.desc() : notification.createdAt.asc(),
				isDesc ? notification.id.desc() : notification.id.asc()
			};
		};
	}
}
