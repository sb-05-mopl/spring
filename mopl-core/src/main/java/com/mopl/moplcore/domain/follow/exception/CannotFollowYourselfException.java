package com.mopl.moplcore.domain.follow.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class CannotFollowYourselfException extends BaseException {
	public CannotFollowYourselfException() {
		super(ErrorCode.CANNOT_FOLLOW_YOURSELF);
	}
}
