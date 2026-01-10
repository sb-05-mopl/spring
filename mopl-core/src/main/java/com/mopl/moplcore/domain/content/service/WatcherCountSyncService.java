package com.mopl.moplcore.domain.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatcherCountSyncService {

    private final StringRedisTemplate redisTemplate;
    private final ContentSyncService contentSyncService;

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

        log.debug("watcherCount 비동기 동기화 완료: {} 건", syncCount);
    }
}