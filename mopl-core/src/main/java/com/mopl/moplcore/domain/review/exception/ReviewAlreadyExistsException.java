package com.mopl.moplcore.domain.review.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class ReviewAlreadyExistsException extends BaseException {
	public ReviewAlreadyExistsException() {
		super(ErrorCode.REVIEW_ALREADY_EXISTS);
	}
}