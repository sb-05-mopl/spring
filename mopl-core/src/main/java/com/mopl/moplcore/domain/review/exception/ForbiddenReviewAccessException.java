package com.mopl.moplcore.domain.review.exception;

import java.util.UUID;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class ForbiddenReviewAccessException extends BaseException {

	public ForbiddenReviewAccessException() {
		super(ErrorCode.FORBIDDEN_REVIEW_ACCESS);
	}

	public ForbiddenReviewAccessException(Throwable cause) {
		super(ErrorCode.FORBIDDEN_REVIEW_ACCESS, cause);
	}

	public static ForbiddenReviewAccessException withDetails(UUID reviewId, UUID requesterId, UUID ownerId) {
		return (ForbiddenReviewAccessException) new ForbiddenReviewAccessException()
			.addDetail("reviewId", reviewId)
			.addDetail("requesterId", requesterId)
			.addDetail("ownerId", ownerId);
	}
}
