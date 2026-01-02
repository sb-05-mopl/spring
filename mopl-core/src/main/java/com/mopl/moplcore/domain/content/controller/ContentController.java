package com.mopl.moplcore.domain.content.controller;

import java.rmi.server.UID;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mopl.moplcore.domain.content.dto.ContentCreateRequest;
import com.mopl.moplcore.domain.content.dto.ContentDto;
import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.dto.ContentUpdateRequest;
import com.mopl.moplcore.domain.content.dto.CursorResponseContentDto;
import com.mopl.moplcore.domain.content.service.ContentManagementService;
import com.mopl.moplcore.domain.content.service.ContentSearchService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

	private final ContentSearchService contentSearchService;
	private final ContentManagementService contentManagementService;

	@Operation(summary = "콘텐츠 목록 조회")
	@GetMapping
	public ResponseEntity<CursorResponseContentDto> searchContents(
		@Valid @ModelAttribute ContentSearchRequest request) {
		CursorResponseContentDto response = contentSearchService.searchContents(request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "콘텐츠 상세 조회")
	@GetMapping("/{id}")
	public ResponseEntity<ContentDto> getContent(@PathVariable UUID id) {
		ContentDto response = contentSearchService.getContent(id);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "[어드민] 콘텐츠 생성(URL만)")
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ContentDto> createContent(
		@RequestBody @Valid ContentCreateRequest request
	) {
		ContentDto response = contentManagementService.createContent(request, null);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "[어드민] 콘텐츠 생성(파일 업로드)")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ContentDto> createContentWithFile(
		@RequestPart("request") @Valid ContentCreateRequest request,
		@RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
	) {
		ContentDto response = contentManagementService.createContent(request, thumbnail);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "[어드민] 콘텐츠 수정 (URL만)")
	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ContentDto> updateContent(
		@PathVariable UUID id,
		@RequestBody @Valid ContentUpdateRequest request
	) {
		ContentDto response = contentManagementService.updateContent(id, request, null);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "[어드민] 콘텐츠 수정 (파일 업로드)")
	@PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ContentDto> updateContentWithFile(
		@PathVariable UUID id,
		@RequestPart("request") @Valid ContentUpdateRequest request,
		@RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail
	) {
		ContentDto response = contentManagementService.updateContent(id, request, thumbnail);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "[어드민] 콘텐츠 삭제")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteContent(@PathVariable UUID id) {
		contentManagementService.deleteContent(id);
		return ResponseEntity.ok().build();
	}
}
