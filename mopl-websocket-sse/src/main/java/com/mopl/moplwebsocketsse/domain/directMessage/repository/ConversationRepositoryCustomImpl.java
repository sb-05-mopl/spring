package com.mopl.moplwebsocketsse.domain.directMessage.repository;

import static com.mopl.moplwebsocketsse.domain.directMessage.entity.QConversation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.QConversationParticipants;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryCustomImpl implements ConversationRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	private final QConversationParticipants cp1 = new QConversationParticipants("cp1");
	private final QConversationParticipants cp2 = new QConversationParticipants("cp2");

	@Override
	public List<Conversation> findByParticipantIdWithCursor(
		UUID participantId,
		String keywordLike,
		String cursor,
		UUID idAfter,
		int limit,
		String sortDirection
	) {
		return queryFactory
			.selectFrom(conversation)
			.join(cp1).on(cp1.conversation.eq(conversation))
			.join(cp2).on(cp2.conversation.eq(conversation))
			.where(
				cp1.user.id.eq(participantId),
				cp2.user.id.ne(participantId),
				keywordLikeCondition(keywordLike),
				cursorCondition(cursor, idAfter, sortDirection)
			)
			.orderBy(orderSpecifier(sortDirection))
			.limit(limit + 1)
			.fetch();
	}

	@Override
	public long countByParticipantId(UUID participantId, String keywordLike) {
		Long count = queryFactory
			.select(conversation.count())
			.from(conversation)
			.join(cp1).on(cp1.conversation.eq(conversation))
			.join(cp2).on(cp2.conversation.eq(conversation))
			.where(
				cp1.user.id.eq(participantId),
				cp2.user.id.ne(participantId),
				keywordLikeCondition(keywordLike)
			)
			.fetchOne();

		return count != null ? count : 0L;
	}

	private BooleanExpression keywordLikeCondition(String keywordLike) {
		if (keywordLike == null || keywordLike.isBlank()) {
			return null;
		}
		return cp2.user.name.containsIgnoreCase(keywordLike);
	}

	private BooleanExpression cursorCondition(String cursor, UUID idAfter, String sortDirection) {
		if (cursor == null || idAfter == null) {
			return null;
		}

		Instant cursorTime = Instant.parse(cursor);
		boolean isDesc = "DESCENDING".equals(sortDirection);

		if (isDesc) {
			return conversation.createdAt.lt(cursorTime)
				.or(conversation.createdAt.eq(cursorTime)
					.and(conversation.id.lt(idAfter)));
		} else {
			return conversation.createdAt.gt(cursorTime)
				.or(conversation.createdAt.eq(cursorTime)
					.and(conversation.id.gt(idAfter)));
		}
	}

	private OrderSpecifier<?>[] orderSpecifier(String sortDirection) {
		boolean isDesc = "DESCENDING".equals(sortDirection);

		OrderSpecifier<?> createdAtSort = isDesc ? conversation.createdAt.desc() : conversation.createdAt.asc();
		OrderSpecifier<?> idSort = isDesc ? conversation.id.desc() : conversation.id.asc();

		return new OrderSpecifier[] {createdAtSort, idSort};
	}
}