package com.mopl.moplcore.domain.review.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplcore.domain.review.dto.CursorResponseReviewDto;
import com.mopl.moplcore.domain.review.dto.ReviewCreateRequest;
import com.mopl.moplcore.domain.review.dto.ReviewDto;
import com.mopl.moplcore.domain.review.dto.ReviewSearchRequest;
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
	private final UUID TEMP_AUTHOR_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

	@PostMapping
	public ResponseEntity<ReviewDto> create(@RequestBody @Valid ReviewCreateRequest request) {
		ReviewDto response = reviewService.create(request, TEMP_AUTHOR_ID);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<CursorResponseReviewDto> findReviews(@Valid ReviewSearchRequest request) {
		CursorResponseReviewDto response = reviewService.findReviews(request);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{reviewId}")
	public ResponseEntity<ReviewDto> update(
		@PathVariable UUID reviewId,
		@RequestBody @Valid ReviewUpdateRequest request
	) {
		ReviewDto response = reviewService.update(reviewId, request, TEMP_AUTHOR_ID);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> delete(@PathVariable UUID reviewId) {
		reviewService.delete(reviewId, TEMP_AUTHOR_ID);

		return ResponseEntity.noContent().build();
	}
}
