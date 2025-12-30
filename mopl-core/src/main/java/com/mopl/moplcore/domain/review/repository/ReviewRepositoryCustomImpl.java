package com.mopl.moplcore.domain.review.repository;

import static com.mopl.moplcore.domain.review.entity.QReview.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.mopl.moplcore.domain.review.entity.Review;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Review> findByContentIdWithCursor(
		UUID contentId,
		String cursor,
		UUID idAfter,
		int limit,
		String sortBy,
		String sortDirection
	) {
		return queryFactory
			.selectFrom(review)
			.where(
				contentIdEq(contentId),
				cursorCondition(cursor, idAfter, sortBy, sortDirection)
			)
			.orderBy(orderSpecifier(sortBy, sortDirection))
			.limit(limit + 1)
			.fetch();

	}

	private BooleanExpression contentIdEq(UUID contentId) {
		return contentId != null ? review.content.id.eq(contentId) : null;
	}

	private BooleanExpression cursorCondition(String cursor, UUID idAfter, String sortBy, String sortDirection) {
		if (cursor == null || idAfter == null) {
			return null;
		}

		return switch (sortBy) {
			case "rating" -> ratingCursorCondition(cursor, idAfter, sortDirection);
			default -> createdAtCursorCondition(cursor, idAfter, sortDirection);
		};
	}

	private BooleanExpression createdAtCursorCondition(String cursor, UUID idAfter, String sortDirection) {
		Instant cursorTime = Instant.parse(cursor);
		boolean isDesc = "DESCENDING".equals(sortDirection);

		if (isDesc) {
			return review.createdAt.lt(cursorTime)
				.or(review.createdAt.eq(cursorTime)
					.and(review.id.lt(idAfter)));
		} else {
			return review.createdAt.gt(cursorTime)
				.or(review.createdAt.eq(cursorTime)
					.and(review.id.gt(idAfter)));
		}
	}

	private BooleanExpression ratingCursorCondition(String cursor, UUID idAfter, String sortDirection) {
		double ratingCursor = Double.parseDouble(cursor);
		boolean isDesc = "DESCENDING".equals(sortDirection);

		if (isDesc) {
			return review.rating.lt(ratingCursor)
				.or(review.rating.eq(ratingCursor)
					.and(review.id.lt(idAfter)));
		} else {
			return review.rating.gt(ratingCursor)
				.or(review.rating.eq(ratingCursor)
					.and(review.id.gt(idAfter)));
		}
	}

	private OrderSpecifier<?>[] orderSpecifier(String sortBy, String sortDirection) {
		boolean isDesc = "DESCENDING".equals(sortDirection);

		OrderSpecifier<?> sortField = switch (sortBy) {
			case "rating" -> isDesc ? review.rating.desc() : review.rating.asc();
			default -> isDesc ? review.createdAt.desc() : review.createdAt.asc();
		};

		OrderSpecifier<?> idSort = isDesc ? review.id.desc() : review.id.asc();

		return new OrderSpecifier[] {sortField, idSort};
	}
}