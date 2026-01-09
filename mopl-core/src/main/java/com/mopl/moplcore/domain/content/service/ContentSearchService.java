package com.mopl.moplcore.domain.content.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import com.mopl.moplcore.domain.content.document.ContentDocument;
import com.mopl.moplcore.domain.content.dto.ContentDto;
import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.dto.CursorResponseContentDto;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.Type;
import com.mopl.moplcore.domain.content.exception.ContentNotFoundException;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentSearchRepository;
import com.mopl.moplcore.domain.watch.repository.WatchingSessionReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSearchService {

	private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

	private final ContentSearchRepository contentSearchRepository;
	private final ContentRepository contentRepository;
	private final ElasticsearchOperations elasticsearchOperations;
	private final WatchingSessionReader watchingSessionReader;
	private final WatcherCountSyncService watcherCountSyncService;

	public CursorResponseContentDto searchContents(ContentSearchRequest request) {

		if (request.getSortBy() == ContentSearchRequest.SortBy.watcherCount) {
			watcherCountSyncService.syncWatcherCountsAsync();
		}

		Criteria criteria = buildCriteria(request);

		Sort sort = buildSort(request);

		Query query = new CriteriaQuery(criteria).setPageable(PageRequest.of(0, request.getLimit() + 1, sort));
		SearchHits<ContentDocument> searchHits = elasticsearchOperations.search(query, ContentDocument.class);

		List<ContentDocument> documents = searchHits.getSearchHits().stream().map(SearchHit::getContent).toList();

		long totalCount = elasticsearchOperations.count(new CriteriaQuery(criteria), ContentDocument.class);

		boolean hasNext = documents.size() > request.getLimit();
		if (hasNext) {
			documents = documents.subList(0, request.getLimit());
		}

		List<ContentDto> contentDtos = documents.stream().map(this::toDto).toList();

		String nextCursor = null;
		if (hasNext && !documents.isEmpty()) {
			ContentDocument lastDoc = documents.get(documents.size() - 1);
			Instant instant = lastDoc.getCreatedAt().atStartOfDay(ZoneId.systemDefault()).toInstant();
			nextCursor = CursorResponseContentDto.encodeCursor(UUID.fromString(lastDoc.getId()), instant);
		}

		return CursorResponseContentDto.builder()
			.data(contentDtos)
			.nextCursor(nextCursor)
			.nextIdAfter(
				hasNext && !documents.isEmpty() ? UUID.fromString(documents.get(documents.size() - 1).getId()) : null)
			.hasNext(hasNext)
			.totalCount((int)totalCount)
			.sortBy(request.getSortBy().name())
			.sortDirection(request.getSortDirection().name())
			.build();
	}

	public ContentDto getContent(UUID id) {
		Content content = contentRepository.findById(id).orElseThrow(() -> new ContentNotFoundException(id));

		return toDto(content);
	}

	private Criteria buildCriteria(ContentSearchRequest request) {
		List<Criteria> criteriaList = new ArrayList<>();

		if (request.getKeywordLike() != null && !request.getKeywordLike().trim().isEmpty()) {
			Criteria titleCriteria = Criteria.where("title").matches(request.getKeywordLike());
			Criteria descCriteria = Criteria.where("description").matches(request.getKeywordLike());
			criteriaList.add(titleCriteria.or(descCriteria));
		}

		if (request.getTypeEqual() != null) {
			criteriaList.add(Criteria.where("type").is(request.getTypeEqual()));
		}

		if (request.getTagsIn() != null && !request.getTagsIn().isEmpty()) {
			criteriaList.add(Criteria.where("tags").in(request.getTagsIn()));
		}

		if (request.getCursor() != null && !request.getCursor().isBlank()) {
			try {
				CursorResponseContentDto.Cursor cursor = CursorResponseContentDto.decodeCursor(request.getCursor());

				java.time.LocalDate cursorDate = java.time.LocalDate.ofInstant(cursor.createdAt(),
					ZoneId.systemDefault());

				boolean isAsc = request.getSortDirection() == ContentSearchRequest.SortDirection.ASCENDING;

				if (request.getSortBy() == ContentSearchRequest.SortBy.createdAt) {
					if (isAsc) {
						criteriaList.add(Criteria.where("createdAt").greaterThan(cursorDate));
					} else {
						criteriaList.add(Criteria.where("createdAt").lessThan(cursorDate));
					}
				}
			} catch (IllegalArgumentException e) {
				log.warn("Invalid cursor: {}", request.getCursor());
			}
		}

		if (criteriaList.isEmpty()) {
			return new Criteria();
		}

		Criteria result = criteriaList.get(0);
		for (int i = 1; i < criteriaList.size(); i++) {
			result = result.and(criteriaList.get(i));
		}

		return result;
	}

	private Sort buildSort(ContentSearchRequest request) {
		boolean isAsc = request.getSortDirection() == ContentSearchRequest.SortDirection.ASCENDING;
		Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;

		return switch (request.getSortBy()) {
			case createdAt -> Sort.by(direction, "createdAt");
			case rate -> Sort.by(direction, "averageRating");
			case watcherCount -> Sort.by(direction, "watcherCount");
		};
	}

	private ContentDto toDto(ContentDocument document) {
		String fullThumbnailUrl = buildImageUrl(document.getType(), document.getThumbnailUrl());

		UUID contentId = UUID.fromString(document.getId());
		long watcherCount = watchingSessionReader.countByContentId(contentId);

		return ContentDto.builder()
			.id(UUID.fromString(document.getId()))
			.type(document.getType())
			.title(document.getTitle())
			.description(document.getDescription())
			.thumbnailUrl(fullThumbnailUrl)
			.tags(document.getTags() != null ? document.getTags() : List.of())
			.averageRating(document.getAverageRating() != null ? document.getAverageRating() : 0.0)
			.reviewCount(document.getReviewCount() != null ? document.getReviewCount() : 0)
			.watcherCount(watcherCount)
			.build();
	}

	private ContentDto toDto(Content content) {

		long watcherCount = watchingSessionReader.countByContentId(content.getId());

		return ContentDto.builder()
			.id(content.getId())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(buildImageUrl(content.getType(), content.getThumbnailUrl()))
			.tags(List.of())
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.watcherCount(watcherCount)
			.build();
	}

	private String buildImageUrl(Type type, String path) {
		if (path == null) {
			return null;
		}

		if (path.startsWith("http://") || path.startsWith("https://")) {
			return path;
		}

		return switch (type) {
			case MOVIE, TV_SERIES -> TMDB_IMAGE_BASE_URL + path;
			case SPORTS -> path;
		};
	}
}