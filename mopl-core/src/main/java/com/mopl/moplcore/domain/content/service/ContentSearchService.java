package com.mopl.moplcore.domain.content.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.mopl.moplcore.domain.content.document.ContentDocument;
import com.mopl.moplcore.domain.content.dto.ContentDto;
import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.dto.CursorResponseContentDto;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.Type;
import com.mopl.moplcore.domain.content.exception.ContentNotFoundException;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentTagRepository;
import com.mopl.moplcore.domain.watch.repository.WatchingSessionReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSearchService {

	private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;
	private final ElasticsearchOperations elasticsearchOperations;
	private final WatchingSessionReader watchingSessionReader;
	private final WatcherCountSyncService watcherCountSyncService;
	private final ContentSyncService contentSyncService;

	public CursorResponseContentDto searchContents(ContentSearchRequest request) {
		if (request.getSortBy() == ContentSearchRequest.SortBy.watcherCount) {
			watcherCountSyncService.syncWatcherCountsAsync();
		}
		contentSyncService.syncAllContents(); // 리뷰 CUD 시 contentSyncService.syncContent(UUID contentId) 호출 방식으로 변경 필요

		BoolQuery.Builder mainBoolQuery = buildBoolQuery(request);

		var queryBuilder = NativeQuery.builder()
			.withQuery(q -> q.bool(mainBoolQuery.build()))
			.withMaxResults(request.getLimit() + 1);

		boolean isAsc = request.getSortDirection() == ContentSearchRequest.SortDirection.ASCENDING;
		SortOrder order = isAsc ? SortOrder.Asc : SortOrder.Desc;

		switch (request.getSortBy()) {
			case createdAt -> queryBuilder.withSort(s -> s.field(f -> f.field("createdAt").order(order)));
			case rate -> {
				queryBuilder.withSort(s -> s.field(f -> f.field("averageRating").order(order)));
				queryBuilder.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
			}
			case watcherCount -> {
				queryBuilder.withSort(s -> s.field(f -> f.field("watcherCount").order(order)));
				queryBuilder.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
			}
		}

		SearchHits<ContentDocument> searchHits = elasticsearchOperations.search(
			queryBuilder.build(), ContentDocument.class);

		List<ContentDocument> documents = searchHits.getSearchHits()
			.stream().map(SearchHit::getContent).toList();

		long totalCount = elasticsearchOperations.count(
			NativeQuery.builder().withQuery(q -> q.bool(buildBasicFilters(request).build())).build(),
			ContentDocument.class
		);

		boolean hasNext = documents.size() > request.getLimit();
		if (hasNext) documents = documents.subList(0, request.getLimit());

		List<ContentDto> contentDtos = documents.stream().map(this::toDto).toList();
		String nextCursor = generateNextCursor(documents, hasNext, request);

		return CursorResponseContentDto.builder()
			.data(contentDtos).nextCursor(nextCursor).hasNext(hasNext)
			.totalCount(totalCount).sortBy(request.getSortBy().name())
			.sortDirection(request.getSortDirection().name()).build();
	}

	private BoolQuery.Builder buildBoolQuery(ContentSearchRequest request) {
		BoolQuery.Builder boolQuery = buildBasicFilters(request);

		if (request.getCursor() != null && !request.getCursor().isBlank()) {
			try {
				CursorResponseContentDto.Cursor cursor = CursorResponseContentDto.decodeCursor(request.getCursor());
				boolean isAsc = request.getSortDirection() == ContentSearchRequest.SortDirection.ASCENDING;

				switch (request.getSortBy()) {
					case createdAt -> addCreatedAtCursor(boolQuery, cursor, isAsc);
					case rate -> addRateCursor(boolQuery, cursor, isAsc);
					case watcherCount -> addWatcherCountCursor(boolQuery, cursor, isAsc);
				}
			} catch (Exception e) {
				log.error("Cursor decoding error", e);
			}
		}
		return boolQuery;
	}

	private BoolQuery.Builder buildBasicFilters(ContentSearchRequest request) {
		BoolQuery.Builder boolQuery = new BoolQuery.Builder();

		if (request.getKeywordLike() != null && !request.getKeywordLike().trim().isEmpty()) {
			String keyword = request.getKeywordLike();
			boolQuery.must(m -> m.bool(b -> b
				.should(s -> s.match(mt -> mt.field("title").query(keyword)))
				.should(s -> s.match(mt -> mt.field("description").query(keyword)))
				.minimumShouldMatch("1")
			));
		}

		if (request.getTypeEqual() != null) {
			boolQuery.filter(f -> f.term(t -> t.field("type").value(request.getTypeEqual().name())));
		}

		if (request.getTagsIn() != null && !request.getTagsIn().isEmpty()) {
			List<FieldValue> tags = request.getTagsIn().stream().map(FieldValue::of).toList();
			boolQuery.filter(f -> f.terms(t -> t.field("tags").terms(ts -> ts.value(tags))));
		}

		return boolQuery;
	}

	private void addCreatedAtCursor(
		BoolQuery.Builder boolQuery,
		CursorResponseContentDto.Cursor cursor,
		boolean isAsc
	) {
		String createdAt = cursor.createdAt().toString();

		boolQuery.filter(f -> f.range(r -> r.date(d -> {
			d.field("createdAt");
			if (isAsc) {
				d.gt(createdAt);
			} else {
				d.lt(createdAt);
			}
			return d;
		})));
	}
	private void addRateCursor(
		BoolQuery.Builder boolQuery,
		CursorResponseContentDto.Cursor cursor,
		boolean isAsc
	) {
		Double rate = cursor.averageRating() != null ? cursor.averageRating() : 0.0;
		String createdAt = cursor.createdAt().toString();

		boolQuery.filter(f -> f.bool(b -> b.minimumShouldMatch("1")

			.should(s -> s.range(r -> r.number(n -> {
				n.field("averageRating");
				if (isAsc) {
					n.gt(rate);
				} else {
					n.lt(rate);
				}
				return n;
			})))

			.should(s -> s.bool(sb -> sb
				.must(m -> m.term(t -> t.field("averageRating").value(rate)))
				.must(m -> m.range(r -> r.date(d -> {
					d.field("createdAt");
					if (isAsc) {
						d.gt(createdAt);
					} else {
						d.lt(createdAt);
					}
					return d;
				})))
			))
		));
	}


	private void addWatcherCountCursor(
		BoolQuery.Builder boolQuery,
		CursorResponseContentDto.Cursor cursor,
		boolean isAsc
	) {
		Double count = cursor.watcherCount() != null
			? cursor.watcherCount().doubleValue()
			: 0.0;
		String createdAt = cursor.createdAt().toString();

		boolQuery.filter(f -> f.bool(b -> b.minimumShouldMatch("1")

			.should(s -> s.range(r -> r.number(n -> {
				n.field("watcherCount");
				if (isAsc) {
					n.gt(count);
				} else {
					n.lt(count);
				}
				return n;
			})))

			.should(s -> s.bool(sb -> sb
				.must(m -> m.term(t -> t.field("watcherCount").value(count)))
				.must(m -> m.range(r -> r.date(d -> {
					d.field("createdAt");
					if (isAsc) {
						d.gt(createdAt);
					} else {
						d.lt(createdAt);
					}
					return d;
				})))
			))
		));
	}

	private String generateNextCursor(List<ContentDocument> docs, boolean hasNext, ContentSearchRequest request) {
		if (!hasNext || docs.isEmpty()) return null;
		ContentDocument last = docs.get(docs.size() - 1);

		var cursor = new CursorResponseContentDto.Cursor(
			UUID.fromString(last.getContentId()),
			last.getCreatedAt(),
			request.getSortBy() == ContentSearchRequest.SortBy.rate ? last.getAverageRating() : null,
			request.getSortBy() == ContentSearchRequest.SortBy.watcherCount ? last.getWatcherCount() : null
		);
		return CursorResponseContentDto.encodeCursor(cursor);
	}

	private ContentDto toDto(ContentDocument document) {
		return ContentDto.builder()
			.id(UUID.fromString(document.getContentId()))
			.type(document.getType())
			.title(document.getTitle())
			.description(document.getDescription())
			.thumbnailUrl(buildImageUrl(document.getType(), document.getThumbnailUrl()))
			.tags(document.getTags() != null ? document.getTags() : List.of())
			.averageRating(document.getAverageRating() != null ? document.getAverageRating() : 0.0)
			.reviewCount(document.getReviewCount() != null ? document.getReviewCount() : 0)
			.watcherCount(watchingSessionReader.countByContentId(UUID.fromString(document.getContentId())))
			.build();
	}

	private String buildImageUrl(Type type, String path) {
		if (path == null) return null;
		if (path.startsWith("http")) return path;
		return (type == Type.MOVIE || type == Type.TV_SERIES) ? TMDB_IMAGE_BASE_URL + path : path;
	}

	public ContentDto getContent(UUID id) {
		Content content = contentRepository.findById(id).orElseThrow(() -> new ContentNotFoundException(id));
		return toDto(content);
	}

	private ContentDto toDto(Content content) {
		List<String> tags = contentTagRepository.findByContentId(content.getId())
			.stream()
			.map(ct -> ct.getTag().getName())
			.toList();

		return ContentDto.builder()
			.id(content.getId())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(buildImageUrl(content.getType(), content.getThumbnailUrl()))
			.tags(tags)
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.watcherCount(watchingSessionReader.countByContentId(content.getId()))
			.build();
	}
}