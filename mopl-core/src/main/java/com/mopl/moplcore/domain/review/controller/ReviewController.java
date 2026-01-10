package com.mopl.moplcore.domain.review.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.mopl.moplcore.security.core.principal.MoplUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<ReviewDto> create(
		@RequestBody @Valid ReviewCreateRequest request,
		@AuthenticationPrincipal MoplUserDetails userDetails
	) {
		UUID authorId = userDetails.getUserDto().getId();
		ReviewDto response = reviewService.create(request, authorId);

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
		@RequestBody @Valid ReviewUpdateRequest request,
		@AuthenticationPrincipal MoplUserDetails userDetails
	) {
		UUID authorId = userDetails.getUserDto().getId();
		ReviewDto response = reviewService.update(reviewId, request, authorId);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> delete(
		@PathVariable UUID reviewId,
		@AuthenticationPrincipal MoplUserDetails userDetails
	) {
		UUID authorId = userDetails.getUserDto().getId();
		reviewService.delete(reviewId, authorId);

		return ResponseEntity.noContent().build();
	}
}