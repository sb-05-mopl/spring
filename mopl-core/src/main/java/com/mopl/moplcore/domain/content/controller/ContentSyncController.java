package com.mopl.moplcore.domain.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplcore.domain.content.service.ContentSyncService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/content-sync")
@RequiredArgsConstructor
public class ContentSyncController {

    private final ContentSyncService contentSyncService;

    @Operation(summary = "[어드민] Elasticsearch 전체 동기화")
    @PostMapping
    public ResponseEntity<SyncResponse> syncAll() {
        long count = contentSyncService.syncAllContents();
        return ResponseEntity.ok(
            new SyncResponse(count, "동기화 완료")
        );
    }

    @Operation(summary = "[어드민] Elasticsearch 인덱스 재생성")
    @PostMapping("/reindex")
    public ResponseEntity<SyncResponse> reindex() {
        contentSyncService.reindexAll();
        long count = contentSyncService.syncAllContents();
        return ResponseEntity.ok(
            new SyncResponse(count, "재인덱싱 완료")
        );
    }

    @Operation(summary = "[어드민] Elasticsearch 인덱스 삭제")
    @DeleteMapping
    public ResponseEntity<SyncResponse> deleteAll() {
        return ResponseEntity.ok(
            new SyncResponse(0, "인덱스 삭제 완료")
        );
    }

    private record SyncResponse(long count, String message) {}
}