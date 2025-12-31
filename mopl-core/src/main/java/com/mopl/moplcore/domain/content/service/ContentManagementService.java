package com.mopl.moplcore.domain.content.service;

import com.mopl.moplcore.domain.content.dto.ContentCreateRequest;
import com.mopl.moplcore.domain.content.dto.ContentDto;
import com.mopl.moplcore.domain.content.dto.ContentUpdateRequest;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.ContentTag;
import com.mopl.moplcore.domain.content.entity.Tag;
import com.mopl.moplcore.domain.content.exception.ContentNotFoundException;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentTagRepository;
import com.mopl.moplcore.domain.content.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentManagementService {

	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;
	private final TagRepository tagRepository;

	@Transactional
	public ContentDto createContent(ContentCreateRequest request) {
		Content content = Content.builder()
			.type(request.type())
			.title(request.title())
			.description(request.description())
			.sourceId(request.sourceId())
			.thumbnailUrl(request.thumbnailUrl())
			.build();

		Content savedContent = contentRepository.save(content);

		if (request.tags() != null && !request.tags().isEmpty()) {
			List<ContentTag> contentTags = request.tags().stream()
				.map(tagName -> {
					Tag tag = tagRepository.findByName(tagName)
						.orElseGet(() -> tagRepository.save(new Tag(tagName)));

					return ContentTag.builder()
						.content(savedContent)
						.tag(tag)
						.build();
				})
				.toList();
			contentTagRepository.saveAll(contentTags);
		}

		return toDto(savedContent);
	}

	@Transactional
	public ContentDto updateContent(UUID id, ContentUpdateRequest request) {
		Content content = contentRepository.findById(id)
			.orElseThrow(() -> new ContentNotFoundException(id));

		if (request.title() != null) {
			content.updateTitle(request.title());
		}
		if (request.description() != null) {
			content.updateDescription(request.description());
		}
		if (request.thumbnailUrl() != null) {
			content.updateThumbnailUrl(request.thumbnailUrl());
		}

		if (request.tags() != null) {
			contentTagRepository.deleteByContentId(id);
			contentTagRepository.flush();

			List<ContentTag> contentTags = request.tags().stream()
				.map(tagName -> {
					Tag tag = tagRepository.findByName(tagName)
						.orElseGet(() -> tagRepository.save(new Tag(tagName)));

					return ContentTag.builder()
						.content(content)
						.tag(tag)
						.build();
				})
				.toList();
			contentTagRepository.saveAll(contentTags);
		}

		return toDto(content);
	}

	@Transactional
	public void deleteContent(UUID id) {
		Content content = contentRepository.findById(id)
			.orElseThrow(() -> new ContentNotFoundException(id));
		contentRepository.delete(content);
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