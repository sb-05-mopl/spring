package com.mopl.moplwebsocketsse.domain.directMessage.repository;

import static com.mopl.moplwebsocketsse.domain.directMessage.entity.QDirectMessage.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.mopl.moplwebsocketsse.domain.directMessage.entity.DirectMessage;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DirectMessageRepositoryCustomImpl implements DirectMessageRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<DirectMessage> findByConversationIdWithCursor(
		UUID conversationId,
		String cursor,
		UUID idAfter,
		int limit,
		String sortDirection
	) {
		return queryFactory
			.selectFrom(directMessage)
			.join(directMessage.sender).fetchJoin()
			.where(
				directMessage.conversation.id.eq(conversationId),
				cursorCondition(cursor, idAfter, sortDirection)
			)
			.orderBy(orderSpecifier(sortDirection))
			.limit(limit + 1)
			.fetch();
	}

	@Override
	public long countByConversationId(UUID conversationId) {
		Long count = queryFactory
			.select(directMessage.count())
			.from(directMessage)
			.where(directMessage.conversation.id.eq(conversationId))
			.fetchOne();

		return count != null ? count : 0L;
	}

	private BooleanExpression cursorCondition(String cursor, UUID idAfter, String sortDirection) {
		if (cursor == null || idAfter == null) {
			return null;
		}

		Instant cursorTime = Instant.parse(cursor);
		boolean isDesc = "DESCENDING".equals(sortDirection);

		if (isDesc) {
			return directMessage.createdAt.lt(cursorTime)
				.or(directMessage.createdAt.eq(cursorTime)
					.and(directMessage.id.lt(idAfter)));
		} else {
			return directMessage.createdAt.gt(cursorTime)
				.or(directMessage.createdAt.eq(cursorTime)
					.and(directMessage.id.gt(idAfter)));
		}
	}

	private OrderSpecifier<?>[] orderSpecifier(String sortDirection) {
		boolean isDesc = "DESCENDING".equals(sortDirection);

		OrderSpecifier<?> createdAtSort = isDesc ? directMessage.createdAt.desc() : directMessage.createdAt.asc();
		OrderSpecifier<?> idSort = isDesc ? directMessage.id.desc() : directMessage.id.asc();

		return new OrderSpecifier[] {createdAtSort, idSort};
	}
}