package com.mopl.moplcore.domain.follow.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class ForbiddenFollowAccessException extends BaseException {
	public ForbiddenFollowAccessException() {
		super(ErrorCode.FORBIDDEN_FOLLOW_ACCESS);
	}
}
