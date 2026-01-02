package com.mopl.moplcore.domain.content.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.content.dto.ContentDto;
import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.dto.CursorResponseContentDto;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentTagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentSearchService {
	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;

	public CursorResponseContentDto searchContents(ContentSearchRequest request) {
		List<Content> contents = contentRepository.searchContents(request);

		Long totalCount = contentRepository.countContents(request);

		boolean hasNext = contents.size() > request.getLimit();
		if (hasNext) {
			contents = contents.subList(0, request.getLimit());
		}

		List<ContentDto> contentDtos = contents.stream()
			.map(this::toDto)
			.toList();

		String nextCursor = null;
		if (hasNext && !contents.isEmpty()) {
			Content lastContent = contents.get(contents.size() - 1);
			nextCursor = CursorResponseContentDto.encodeCursor(
				lastContent.getId(),
				lastContent.getCreatedAt()
			);
		}

		return CursorResponseContentDto.builder()
			.data(contentDtos)
			.nextCursor(nextCursor)
			.nextIdAfter(hasNext && !contents.isEmpty() ? contents.get(contents.size() - 1).getId() : null)
			.hasNext(hasNext)
			.totalCount(totalCount.intValue())
			.sortBy(request.getSortBy().name())
			.sortDirection(request.getSortDirection().name())
			.build();
	}

	private ContentDto toDto(Content content) {
		List<String> tags = contentTagRepository
			.findByContentId(content.getId())
			.stream()
			.map(ct -> ct.getTag().getName())
			.toList();

		return ContentDto.builder()
			.id(content.getId())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(content.getThumbnailUrl())
			.tags(tags)
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.watcherCount(0)
			.build();
	}
}
