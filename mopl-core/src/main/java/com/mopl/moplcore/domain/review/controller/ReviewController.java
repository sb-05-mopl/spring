package com.mopl.moplcore.domain.review.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplcore.domain.review.dto.ReviewCreateRequest;
import com.mopl.moplcore.domain.review.dto.ReviewDto;
import com.mopl.moplcore.domain.review.dto.ReviewUpdateRequest;
import com.mopl.moplcore.domain.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;

	/*
	TODO:
		인증 기능 구현 후, 임시 Author ID 제거
		AuthenticationPrincipal 어노테이션을 사용하여
		현재 인증된 사용자의 ID를 가져오도록 수정 필요
	*/
	@PostMapping
	public ResponseEntity<ReviewDto> create(@RequestBody @Valid ReviewCreateRequest request) {
		UUID TEMP_AUTHOR_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

		ReviewDto response = reviewService.create(request, TEMP_AUTHOR_ID);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PatchMapping("/{reviewId}")
	public ResponseEntity<ReviewDto> update(
		@PathVariable UUID reviewId,
		@RequestBody @Valid ReviewUpdateRequest request
	) {
		UUID TEMP_AUTHOR_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

		ReviewDto response = reviewService.update(reviewId, request, TEMP_AUTHOR_ID);
		return ResponseEntity.ok(response);
	}
}
