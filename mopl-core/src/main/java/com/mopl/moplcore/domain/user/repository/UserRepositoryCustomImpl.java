package com.mopl.moplcore.domain.user.repository;

import static com.mopl.moplcore.domain.user.entity.QUser.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.mopl.moplcore.domain.user.dto.SortBy;
import com.mopl.moplcore.domain.user.dto.SortDirection;
import com.mopl.moplcore.domain.user.entity.Role;
import com.mopl.moplcore.domain.user.entity.User;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public long countUsers(String emailLike, Role roleEqual, Boolean isLocked) {
		Long count = queryFactory
			.select(user.count())
			.from(user)
			.where(
				emailLikeContains(emailLike),
				roleEq(roleEqual),
				lockedEq(isLocked)
			)
			.fetchOne();

		return count != null ? count : 0L;
	}

	@Override
	public List<User> findUsersWithCursor(
		String emailLike,
		Role roleEqual,
		Boolean isLocked,
		String cursor,
		UUID idAfter,
		int limit,
		SortBy sortBy,
		SortDirection sortDirection
	) {
		return queryFactory
			.selectFrom(user)
			.where(
				emailLikeContains(emailLike),
				roleEq(roleEqual),
				lockedEq(isLocked),
				cursorCondition(cursor, idAfter, sortBy, sortDirection)
			)
			.orderBy(orderSpecifiers(sortBy, sortDirection))
			.limit(limit + 1L)
			.fetch();
	}

	private BooleanExpression emailLikeContains(String emailLike) {
		return emailLike != null && !emailLike.isBlank()
			? user.email.containsIgnoreCase(emailLike)
			: null;
	}

	private BooleanExpression roleEq(Role roleEqual) {
		return roleEqual != null ? user.role.eq(roleEqual) : null;
	}

	private BooleanExpression lockedEq(Boolean isLocked) {
		return isLocked != null ? user.locked.eq(isLocked) : null;
	}

	private BooleanExpression cursorCondition(
		String cursor,
		UUID idAfter,
		SortBy sortBy,
		SortDirection sortDirection
	) {
		if (cursor == null || idAfter == null) {
			return null;
		}

		return switch (sortBy) {
			case name -> stringCursorCondition(user.name, cursor, idAfter, sortDirection);
			case email -> stringCursorCondition(user.email, cursor, idAfter, sortDirection);
			case role -> stringCursorCondition(user.role.stringValue(), cursor, idAfter, sortDirection);
			case isLocked -> lockedCursorCondition(cursor, idAfter, sortDirection);
			case createdAt -> createdAtCursorCondition(cursor, idAfter, sortDirection);
		};
	}

	private BooleanExpression createdAtCursorCondition(String cursor, UUID idAfter,
		SortDirection sortDirection) {
		Instant cursorTime = Instant.parse(cursor);
		boolean isDesc = sortDirection == SortDirection.DESCENDING;

		if (isDesc) {
			return user.createdAt.lt(cursorTime)
				.or(user.createdAt.eq(cursorTime).and(user.id.lt(idAfter)));
		}
		return user.createdAt.gt(cursorTime)
			.or(user.createdAt.eq(cursorTime).and(user.id.gt(idAfter)));
	}

	private BooleanExpression stringCursorCondition(
		StringExpression field,
		String cursor,
		UUID idAfter,
		SortDirection sortDirection
	) {
		boolean isDesc = sortDirection == SortDirection.DESCENDING;

		if (isDesc) {
			return field.lt(cursor)
				.or(field.eq(cursor).and(user.id.lt(idAfter)));
		}
		return field.gt(cursor)
			.or(field.eq(cursor).and(user.id.gt(idAfter)));
	}

	private NumberExpression<Integer> lockedAsInt() {
		return Expressions.numberTemplate(
			Integer.class,
			"case when {0} = true then 1 else 0 end",
			user.locked
		);
	}

	private BooleanExpression lockedCursorCondition(String cursor, UUID idAfter,
		SortDirection sortDirection) {
		int cursorVal = Boolean.parseBoolean(cursor) ? 1 : 0;
		boolean isDesc = sortDirection == SortDirection.DESCENDING;

		NumberExpression<Integer> lockedVal = lockedAsInt();

		if (isDesc) {
			return lockedVal.lt(cursorVal)
				.or(lockedVal.eq(cursorVal).and(user.id.lt(idAfter)));
		}
		return lockedVal.gt(cursorVal)
			.or(lockedVal.eq(cursorVal).and(user.id.gt(idAfter)));
	}

	private OrderSpecifier<?>[] orderSpecifiers(SortBy sortBy, SortDirection sortDirection) {
		boolean isDesc = sortDirection == SortDirection.DESCENDING;

		OrderSpecifier<?> primary = switch (sortBy) {
			case name -> isDesc ? user.name.desc() : user.name.asc();
			case email -> isDesc ? user.email.desc() : user.email.asc();
			case role -> isDesc ? user.role.stringValue().desc() : user.role.stringValue().asc();
			case isLocked -> isDesc ? lockedAsInt().desc() : lockedAsInt().asc();
			case createdAt -> isDesc ? user.createdAt.desc() : user.createdAt.asc();
		};

		OrderSpecifier<?> tieBreaker = isDesc ? user.id.desc() : user.id.asc();

		return new OrderSpecifier[] {primary, tieBreaker};
	}
}
