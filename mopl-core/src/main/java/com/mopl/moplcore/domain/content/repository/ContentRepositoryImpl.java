package com.mopl.moplcore.domain.content.repository;

import static com.mopl.moplcore.domain.content.dto.ContentSearchRequest.SortDirection.*;

import java.util.List;

import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.dto.CursorResponseContentDto;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.QContent;
import com.mopl.moplcore.domain.content.entity.QContentTag;
import com.mopl.moplcore.domain.content.entity.QTag;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Content> searchContents(ContentSearchRequest request) {
		QContent content = QContent.content;

		return queryFactory
			.selectFrom(content)
			.where(
				keywordLike(request.getKeywordLike()),
				typeEqual(request.getTypeEqual()),
				tagsIn(request.getTagsIn()),
				cursorCondition(request)
			)
			.orderBy(getOrderSpecifier(request))
			.limit(request.getLimit() + 1)
			.fetch();
	}

	private BooleanExpression keywordLike(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return null;
		}
		QContent content = QContent.content;
		return content.title.containsIgnoreCase(keyword)
			.or(content.description.containsIgnoreCase(keyword));
	}

	private BooleanExpression typeEqual(com.mopl.moplcore.domain.content.entity.Type type) {
		if (type == null) {
			return null;
		}
		return QContent.content.type.eq(type);
	}

	private BooleanExpression tagsIn(List<String> tags) {
		if (tags == null || tags.isEmpty()) {
			return null;
		}

		QContent content = QContent.content;
		QContentTag contentTag = QContentTag.contentTag;
		QTag tag = QTag.tag;

		return content.id.in(
			JPAExpressions
				.select(contentTag.content.id)
				.from(contentTag)
				.join(contentTag.tag, tag)
				.where(tag.name.in(tags))
		);
	}

	private BooleanExpression cursorCondition(ContentSearchRequest request) {
		if (request.getCursor() == null || request.getCursor().isBlank()) {
			return null;
		}

		QContent content = QContent.content;

		try {
			CursorResponseContentDto.Cursor cursor =
				CursorResponseContentDto.decodeCursor(request.getCursor());

			boolean isAsc = request.getSortDirection() ==
				ASCENDING;

			if (request.getSortBy() == ContentSearchRequest.SortBy.createdAt) {
				if (isAsc) {
					return content.createdAt.gt(cursor.createdAt())
						.or(
							content.createdAt.eq(cursor.createdAt())
								.and(content.id.gt(cursor.id()))
						);
				} else {
					return content.createdAt.lt(cursor.createdAt())
						.or(
							content.createdAt.eq(cursor.createdAt())
								.and(content.id.lt(cursor.id()))
						);
				}
			}

			return null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private OrderSpecifier<?>[] getOrderSpecifier(ContentSearchRequest request) {
		QContent content = QContent.content;
		boolean isAsc = request.getSortDirection() == ASCENDING;

		return switch (request.getSortBy()) {
			case createdAt -> isAsc
				? new OrderSpecifier<?>[] {
				content.createdAt.asc(),
				content.id.asc()
			}
				: new OrderSpecifier<?>[] {
				content.createdAt.desc(),
				content.id.desc()
			};
			case watcherCount -> null; // ES / Redis 기준
			case rate -> new OrderSpecifier<?>[] {
				isAsc ? content.averageRating.asc() : content.averageRating.desc(),
				content.id.asc()
			};
		};
	}

	@Override
	public Long countContents(ContentSearchRequest request) {
		QContent content = QContent.content;

		JPAQuery<Long> query = queryFactory
			.select(content.count())
			.from(content)
			.where(
				keywordLike(request.getKeywordLike()),
				typeEqual(request.getTypeEqual()),
				tagsIn(request.getTagsIn())
			);

		return query.fetchOne();
	}
}