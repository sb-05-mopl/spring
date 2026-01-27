package com.mopl.moplcore.domain.content.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mopl.moplcore.domain.content.service.ContentSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "content.sync.enabled", havingValue = "true", matchIfMissing = true)
public class ContentSyncRunner implements CommandLineRunner {

    private final ContentSyncService contentSyncService;
    @Override
    public void run(String... args) {
        log.info("Content 자동 동기화 시작");
        try {contentSyncService.syncAllContents();
            log.info("Content 자동 동기화 완료");
        } catch (Exception e) {
            log.error("Content 자동 동기화 실패", e);
        }
    }
}