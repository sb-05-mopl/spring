package com.mopl.moplcore.security.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class LockedUserAccessException extends BaseException {
	public LockedUserAccessException() {
		super(ErrorCode.LOCKED_USER_ACCESS);
	}
}
