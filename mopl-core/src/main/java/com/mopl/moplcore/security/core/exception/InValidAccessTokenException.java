package com.mopl.moplcore.security.core.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class InValidAccessTokenException extends BaseException {
	public InValidAccessTokenException() {
		super(ErrorCode.INVALID_ACCESS_TOKEN);
	}
}
