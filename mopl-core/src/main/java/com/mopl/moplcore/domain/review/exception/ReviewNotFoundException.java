package com.mopl.moplcore.domain.review.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class ReviewNotFoundException extends BaseException {
	public ReviewNotFoundException() {
		super(ErrorCode.REVIEW_NOT_FOUND);
	}

	public ReviewNotFoundException(Throwable cause) {
		super(ErrorCode.REVIEW_NOT_FOUND, cause);
	}

	public static ReviewNotFoundException withReviewId(UUID reviewId) {
		return (ReviewNotFoundException) new ReviewNotFoundException()
			.addDetail("reviewId", reviewId);
	}
}
