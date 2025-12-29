package com.mopl.moplcore.domain.review.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class InvalidRatingException extends BaseException {
	public InvalidRatingException() {
		super(ErrorCode.INVALID_RATING);
	}

	public InvalidRatingException(Throwable cause) {
		super(ErrorCode.INVALID_RATING, cause);
	}

	public static InvalidRatingException withRating(double rating) {
		return (InvalidRatingException) new InvalidRatingException()
			.addDetail("inputRating", rating)
			.addDetail("minRating", 1.0)
			.addDetail("maxRating", 5.0);
	}
}
