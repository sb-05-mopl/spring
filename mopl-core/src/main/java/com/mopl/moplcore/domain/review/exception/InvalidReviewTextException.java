package com.mopl.moplcore.domain.review.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class InvalidReviewTextException extends BaseException {

	public InvalidReviewTextException() {
		super(ErrorCode.INVALID_REVIEW_TEXT);
	}
}