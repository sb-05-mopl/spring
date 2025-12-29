package com.mopl.moplcore.domain.content.repository;

import java.util.List;

import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.QContent;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
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
		// 나중에 구현
		return null;
	}

	private BooleanExpression cursorCondition(ContentSearchRequest request) {
		// 나중에 구현
		return null;
	}

	private OrderSpecifier<?> getOrderSpecifier(ContentSearchRequest request) {
		QContent content = QContent.content;

		boolean isAsc = request.getSortDirection() == ContentSearchRequest.SortDirection.ASCENDING;

		return switch (request.getSortBy()) {
			case createdAt -> isAsc ? content.createdAt.asc() : content.createdAt.desc();
			case watcherCount -> throw new UnsupportedOperationException("watcherCount 정렬은 아직 미구현");
			case rate -> isAsc ? content.averageRating.asc() : content.averageRating.desc();
		};
	}
}