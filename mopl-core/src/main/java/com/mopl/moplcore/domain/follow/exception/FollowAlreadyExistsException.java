package com.mopl.moplcore.domain.follow.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class FollowAlreadyExistsException extends BaseException {
	public FollowAlreadyExistsException() {
		super(ErrorCode.FOLLOW_ALREADY_EXISTS);
	}
}
