package com.mopl.moplcore.security.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class InValidRefreshTokenException extends BaseException {
	public InValidRefreshTokenException() {
		super(ErrorCode.INVALID_REFRESH_TOKEN);
	}
}
