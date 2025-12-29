package com.mopl.moplcore.domain.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.dto.CursorResponseContentDto;
import com.mopl.moplcore.domain.content.service.ContentSearchService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

	private final ContentSearchService contentSearchService;

	@Operation(summary = "콘텐츠 목록 조회")
	@GetMapping
	public ResponseEntity<CursorResponseContentDto> searchContents(
		@Valid @ModelAttribute ContentSearchRequest request) {
		CursorResponseContentDto response = contentSearchService.searchContents(request);
		return ResponseEntity.ok(response);
	}

	// @GetMapping("/{contentId}")
	// public ResponseEntity<ContentDto> getContent(@PathVariable UUID contentId) { }

	// @PostMapping
	// public ResponseEntity<ContentDto> createContent(...) { }

	// @PatchMapping("/{contentId}")
	// public ResponseEntity<ContentDto> updateContent(...) { }

	// @DeleteMapping("/{contentId}")
	// public ResponseEntity<Void> deleteContent(...) { }
}
