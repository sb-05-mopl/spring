package com.mopl.moplcore.domain.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mopl.moplcore.domain.content.document.ContentDocument;
import com.mopl.moplcore.domain.content.repository.ContentSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatcherCountSyncService {

	private static final String CONTENT_WATCHERS_KEY_PREFIX = "watch:content:";

	private final StringRedisTemplate redisTemplate;
	private final ContentSyncService contentSyncService;
	private final ElasticsearchOperations elasticsearchOperations;
	private final ContentSearchRepository contentSearchRepository;

    @Async("taskExecutor")
    public void syncWatcherCountsAsync() {
        log.debug("watcherCount 비동기 동기화 시작");

        ScanOptions options = ScanOptions.scanOptions()
            .match("watch:content:*")
            .count(100)
            .build();

        int syncCount = 0;
        try (var cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                try {
                    String contentId = key.replace("watch:content:", "");
                    contentSyncService.updateWatcherCount(UUID.fromString(contentId));
                    syncCount++;
                } catch (Exception e) {
                    log.error("watcherCount 동기화 실패: key={}", key, e);
                }
            }
        }

		int cleanupCount = cleanupStaleWatcherCounts();

		log.debug("watcherCount 비동기 동기화 완료: 활성 {}건, 정리 {}건", syncCount, cleanupCount);
	}

	private int cleanupStaleWatcherCounts() {
		NativeQuery query = NativeQuery.builder()
			.withQuery(q -> q.range(r -> r.number(n -> n.field("watcherCount").gt(0.0))))
			.withMaxResults(500)
			.build();

		int cleanupCount = 0;
		List<ContentDocument> toUpdate = new ArrayList<>();

		try (SearchHitsIterator<ContentDocument> iterator = elasticsearchOperations.searchForStream(query,
			ContentDocument.class)) {
			while (iterator.hasNext()) {
				ContentDocument doc = iterator.next().getContent();
				String key = CONTENT_WATCHERS_KEY_PREFIX + doc.getContentId();
				Boolean exists = redisTemplate.hasKey(key);

				if (Boolean.FALSE.equals(exists)) {
					doc.setWatcherCount(0L);
					toUpdate.add(doc);
					cleanupCount++;

					if (toUpdate.size() >= 500) {
						contentSearchRepository.saveAll(toUpdate);
						toUpdate.clear();
					}
				}
			}
		}

		if (!toUpdate.isEmpty()) {
			contentSearchRepository.saveAll(toUpdate);
		}

		if (cleanupCount > 0) {
			log.debug("stale watcherCount 정리: {}건", cleanupCount);
		}

		return cleanupCount;
	}
}