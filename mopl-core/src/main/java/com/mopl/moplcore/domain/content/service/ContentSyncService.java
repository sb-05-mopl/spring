package com.mopl.moplcore.domain.content.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.content.document.ContentDocument;
import com.mopl.moplcore.domain.content.entity.Content;
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
		log.info("Content 동기화 시작");

		List<Content> contents = contentRepository.findAll();
		log.info("동기화 할 Content 수: {}", contents.size());

		List<ContentDocument> documents = contents.stream().map(this::convertToDocument).collect(Collectors.toList());

		contentSearchRepository.saveAll(documents);

		long count = contentSearchRepository.count();
		log.info("Content 동기화 완료: {} 건", count);

		return count;
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

		return ContentDocument.from(content.getId(), content.getType(), content.getTitle(), content.getDescription(),
			content.getThumbnailUrl(), tags, content.getAverageRating(), content.getReviewCount(), 0L,
			content.getCreatedAt());
	}

	public void updateWatcherCount(UUID contentId) {
		try {
			long watcherCount = watchingSessionReader.countByContentId(contentId);

			contentSearchRepository.findById(contentId.toString()).ifPresent(document -> {
				document.setWatcherCount(watcherCount);
				contentSearchRepository.save(document);
				log.debug("Updated watcherCount for content {}: {}", contentId, watcherCount);
			});
		} catch (Exception e) {
			log.error("Failed to update watcherCount for content {}", contentId, e);
		}
	}
}