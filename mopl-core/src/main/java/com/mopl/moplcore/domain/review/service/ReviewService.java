package com.mopl.moplcore.domain.review.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.review.dto.CursorResponseReviewDto;
import com.mopl.moplcore.domain.review.dto.ReviewCreateRequest;
import com.mopl.moplcore.domain.review.dto.ReviewDto;
import com.mopl.moplcore.domain.review.dto.ReviewSearchRequest;
import com.mopl.moplcore.domain.review.dto.ReviewUpdateRequest;
import com.mopl.moplcore.domain.review.entity.Review;
import com.mopl.moplcore.domain.review.exception.ForbiddenReviewAccessException;
import com.mopl.moplcore.domain.review.exception.ReviewAlreadyExistsException;
import com.mopl.moplcore.domain.review.exception.ReviewNotFoundException;
import com.mopl.moplcore.domain.review.mapper.ReviewMapper;
import com.mopl.moplcore.domain.review.repository.ReviewRepository;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final ContentRepository contentRepository;
	private final ReviewMapper reviewMapper;

	@Transactional
	public ReviewDto create(ReviewCreateRequest request, UUID authorId) {
		if (reviewRepository.existsByAuthorIdAndContentId(authorId, request.contentId())) {
			throw new ReviewAlreadyExistsException();
		}

		/* TODO : User, Content 있는지 확인하는 검증 라인
		    다른 도메인 예외 추가시 변경
		User author = userRepository.findById(authorId)
			.orElseThrow(UserNotFoundException);
		Content content = contentRepository.findById(request.getContentId())
			.orElseThrow(ContentNotFoundException);
		 */

		User author = userRepository.findById(authorId)
			.orElseThrow(() -> new RuntimeException("author not found"));
		Content content = contentRepository.findById(request.contentId())
			.orElseThrow(() -> new RuntimeException("content not found"));

		Review review = Review.builder()
			.author(author)
			.content(content)
			.rating(request.rating())
			.text(request.text())
			.build();

		Review saved = reviewRepository.save(review);

		return reviewMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public CursorResponseReviewDto findReviews(ReviewSearchRequest request) {
		long totalCount = reviewRepository.count(request.contentId());

		List<Review> reviews = reviewRepository.findByContentIdWithCursor(
			request.contentId(),
			request.cursor(),
			request.idAfter(),
			request.limit(),
			request.sortBy().name(),
			request.sortDirection().name()
		);

		boolean hasNext = reviews.size() > request.limit();
		List<Review> reviewsLimit = hasNext ? reviews.subList(0, request.limit()) : reviews;

		List<ReviewDto> reviewDtos = reviewsLimit.stream()
			.map(reviewMapper::toDto)
			.toList();

		String nextCursor = null;
		UUID nextIdAfter = null;

		if (hasNext && !reviewsLimit.isEmpty()) {
			Review lastReview = reviewsLimit.getLast();

			nextCursor = switch (request.sortBy()) {
				case createdAt -> lastReview.getCreatedAt().toString();
				case rating -> String.valueOf(lastReview.getRating());
			};

			nextIdAfter = lastReview.getId();
		}

		return new CursorResponseReviewDto(
			reviewDtos,
			nextCursor,
			nextIdAfter != null ? nextIdAfter.toString() : null,
			hasNext,
			totalCount,
			request.sortBy(),
			request.sortDirection()
		);
	}

	@Transactional
	public ReviewDto update(UUID reviewId, ReviewUpdateRequest request, UUID requesterId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> ReviewNotFoundException.withReviewId(reviewId));

		if (!review.getAuthor().getId().equals(requesterId)) {
			throw new ForbiddenReviewAccessException();
		}

		review.update(request.text(), request.rating());

		return reviewMapper.toDto(review);
	}

	@Transactional
	public void delete(UUID reviewId, UUID requesterId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> ReviewNotFoundException.withReviewId(reviewId));

		if (!review.getAuthor().getId().equals(requesterId)) {
			throw new ForbiddenReviewAccessException();
		}

		reviewRepository.delete(review);
	}
}
