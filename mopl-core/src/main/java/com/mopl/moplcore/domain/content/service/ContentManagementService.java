package com.mopl.moplcore.domain.content.service;

import com.mopl.moplcore.domain.content.dto.ContentCreateRequest;
import com.mopl.moplcore.domain.content.dto.ContentDto;
import com.mopl.moplcore.domain.content.dto.ContentUpdateRequest;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.ContentTag;
import com.mopl.moplcore.domain.content.entity.Tag;
import com.mopl.moplcore.domain.content.entity.Type;
import com.mopl.moplcore.domain.content.exception.ContentNotFoundException;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentTagRepository;
import com.mopl.moplcore.domain.content.repository.TagRepository;
import com.mopl.moplcore.global.service.S3Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentManagementService {

	private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

	private final ContentRepository contentRepository;
	private final ContentTagRepository contentTagRepository;
	private final TagRepository tagRepository;
	private final S3Service s3Service;

	@Transactional
	public ContentDto createContent(ContentCreateRequest request, MultipartFile thumbnail) {
		String thumbnailUrl = request.thumbnailUrl();

		if (thumbnail != null && !thumbnail.isEmpty()) {
			thumbnailUrl = s3Service.upload(thumbnail);
		}

		Content content = Content.builder()
			.type(request.type())
			.title(request.title())
			.description(request.description())
			.sourceId(request.sourceId())
			.thumbnailUrl(thumbnailUrl)
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
	public ContentDto updateContent(UUID id, ContentUpdateRequest request, MultipartFile thumbnail) {
		Content content = contentRepository.findById(id)
			.orElseThrow(() -> new ContentNotFoundException(id));

		String thumbnailUrl = content.getThumbnailUrl();

		if (thumbnail != null && !thumbnail.isEmpty()) {
			if (isS3Url(thumbnailUrl)) {
				s3Service.delete(thumbnailUrl);
			}

			thumbnailUrl = s3Service.upload(thumbnail);
		}
		else if (request.thumbnailUrl() != null) {
			if (isS3Url(thumbnailUrl)) {
				s3Service.delete(thumbnailUrl);
			}

			thumbnailUrl = request.thumbnailUrl();
		}

		if (request.title() != null) {
			content.updateTitle(request.title());
		}
		if (request.description() != null) {
			content.updateDescription(request.description());
		}
		if (thumbnailUrl != null) {
			content.updateThumbnailUrl(thumbnailUrl);
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

		String thumbnailUrl = content.getThumbnailUrl();
		if (isS3Url(thumbnailUrl)) {
			s3Service.delete(thumbnailUrl);
		}

		contentRepository.delete(content);
	}

	private ContentDto toDto(Content content) {
		List<String> tags = contentTagRepository
			.findByContentId(content.getId())
			.stream()
			.map(ct -> ct.getTag().getName())
			.toList();

		String fullThumbnailUrl = buildImageUrl(content.getType(), content.getThumbnailUrl());

		return ContentDto.builder()
			.id(content.getId())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(fullThumbnailUrl)
			.tags(tags)
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.watcherCount(0)
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

	private boolean isS3Url(String url) {
		return url != null
			&& url.startsWith("https://")
			&& url.contains("s3")
			&& url.contains("amazonaws.com");
	}
}