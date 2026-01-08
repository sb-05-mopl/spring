package com.mopl.moplcore.domain.user.exception;

import java.util.UUID;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class ForbiddenUserAccessException extends BaseException {

	public ForbiddenUserAccessException() {
		super(ErrorCode.FORBIDDEN_USER_ACCESS);
	}

	public static ForbiddenUserAccessException withIds(UUID requesterId, UUID targetUserId) {
		ForbiddenUserAccessException ex = new ForbiddenUserAccessException();
		ex.addDetail("requesterId", requesterId);
		ex.addDetail("targetUserId", targetUserId);
		return ex;
	}
}
