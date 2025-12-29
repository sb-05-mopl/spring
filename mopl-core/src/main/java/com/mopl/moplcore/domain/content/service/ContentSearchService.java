package com.mopl.moplcore.domain.content.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.content.dto.ContentDto;
import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.dto.CursorResponseContentDto;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.repository.ContentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentSearchService {
	private final ContentRepository contentRepository;

	public CursorResponseContentDto searchContents(ContentSearchRequest request) {
		List<Content> contents = contentRepository.searchContents(request);

		boolean hasNext = contents.size() > request.getLimit();
		if (hasNext) {
			contents = contents.subList(0, request.getLimit());
		}
		
		List<ContentDto> contentDtos = contents.stream()
			.map(this::toDto)
			.toList();

		String nextCursor = hasNext ? generateNextCursor(contents.get(contents.size() - 1), request) : null;

		return CursorResponseContentDto.builder()
			.data(contentDtos)
			.nextCursor(nextCursor)
			.nextIdAfter(hasNext ? contents.get(contents.size() - 1).getId() : null)
			.hasNext(hasNext)
			.totalCount(0L)  // 전체 개수 조회 구현 필요
			.sortBy(request.getSortBy().name())
			.sortDirection(request.getSortDirection().name())
			.build();
	}

	private ContentDto toDto(Content content) {
		return ContentDto.builder()
			.id(content.getId())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(content.getThumbnailUrl())
			.tags(List.of())
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.watcherCount(0L)
			.build();
	}

	private String generateNextCursor(Content lastContent, ContentSearchRequest request) {
		// 나중에 구현
		return null;
	}
}
