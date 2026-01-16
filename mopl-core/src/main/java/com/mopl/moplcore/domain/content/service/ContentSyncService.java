package com.mopl.moplcore.domain.content.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.content.document.ContentDocument;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.ContentTag;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentSearchRepository;
import com.mopl.moplcore.domain.content.repository.ContentTagRepository;
import com.mopl.moplcore.domain.watch.repository.WatchingSessionReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSyncService {

	private final ContentRepository contentRepository;
	private final ContentSearchRepository contentSearchRepository;
	private final ContentTagRepository contentTagRepository;
	private final WatchingSessionReader watchingSessionReader;

	@Transactional(readOnly = true)
	public long syncAllContents() {
		log.info("=== 동기화 시작 ===");

		contentSearchRepository.deleteAll();
		log.info("기존 ES 인덱스 초기화 완료");

		int pageSize = 1000;
		int pageNumber = 0;
		long totalSyncedCount = 0; // 전체 동기화 개수를 담을 변수
		Page<Content> contentPage;

		do {
			contentPage = contentRepository.findAll(PageRequest.of(pageNumber, pageSize));
			List<Content> contents = contentPage.getContent();

			if (contents.isEmpty())
				break;

			List<UUID> contentIds = contents.stream().map(Content::getId).toList();

			List<ContentTag> allTagsInBatch = contentTagRepository.findByContentIdIn(contentIds);

			Map<UUID, List<String>> tagMap = allTagsInBatch.stream()
				.collect(Collectors.groupingBy(ct -> ct.getContent().getId(),
					Collectors.mapping(ct -> ct.getTag().getName(), Collectors.toList())));

			List<ContentDocument> documents = contents.stream().map(content -> {
				List<String> tags = tagMap.getOrDefault(content.getId(), List.of());
				return convertToDocumentManual(content, tags);
			}).toList();

			contentSearchRepository.saveAll(documents);

			totalSyncedCount += documents.size();
			pageNumber++;

			log.info("진행 상황: 현재까지 {} 건 완료", totalSyncedCount);

		} while (contentPage.hasNext());

		log.info("=== 동기화 최종 완료: 총 {} 건 ===", totalSyncedCount);
		return totalSyncedCount;
	}

	private ContentDocument convertToDocumentManual(Content content, List<String> tags) {
		return ContentDocument.builder()
			.id(content.getId().toString())
			.contentId(content.getId().toString())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(content.getThumbnailUrl())
			.tags(tags)
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.createdAt(content.getCreatedAt())
			.build();
	}

	@Transactional(readOnly = true)
	public void syncContent(UUID contentId) {
		Content content = contentRepository.findById(contentId)
			.orElseThrow(() -> new IllegalArgumentException("Content not found: " + contentId));

		ContentDocument document = convertToDocument(content);
		contentSearchRepository.save(document);

		log.debug("Content 동기화 완료: id={}", contentId);
	}

	public void deleteContent(UUID contentId) {
		contentSearchRepository.deleteById(contentId.toString());
		log.debug("Content 삭제 완료: id={}", contentId);
	}

	public void reindexAll() {
		log.info("전체 인덱스 재생성 시작");

		contentSearchRepository.deleteAll();
		log.info("기존 인덱스 삭제 완료");

		syncAllContents();

		log.info("전체 인덱스 재생성 완료");
	}

	private ContentDocument convertToDocument(Content content) {
		List<String> tags = contentTagRepository.findByContentId(content.getId())
			.stream()
			.map(ct -> ct.getTag().getName())
			.collect(Collectors.toList());

		return ContentDocument.builder()
			.id(content.getId().toString())
			.contentId(content.getId().toString())
			.type(content.getType())
			.title(content.getTitle())
			.description(content.getDescription())
			.thumbnailUrl(content.getThumbnailUrl())
			.tags(tags)
			.averageRating(content.getAverageRating())
			.reviewCount(content.getReviewCount())
			.watcherCount(0L)
			.createdAt(content.getCreatedAt())
			.build();
	}

	public void updateWatcherCount(UUID contentId) {
		try {
			long watcherCount = watchingSessionReader.countByContentId(contentId);

			contentSearchRepository.findById(contentId.toString()).ifPresent(document -> {
				document.setWatcherCount(watcherCount);
				contentSearchRepository.save(document);
				log.debug("content의 업데이트 된 watcherCount {}: {}", contentId, watcherCount);
			});
		} catch (Exception e) {
			log.error("watcherCount 업데이트 실패 {}", contentId, e);
		}
	}
}