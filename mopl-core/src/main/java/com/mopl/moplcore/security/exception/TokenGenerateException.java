package com.mopl.moplcore.security.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class TokenGenerateException extends BaseException {

	public TokenGenerateException() {
		super(ErrorCode.TOKEN_GENERATE_FAIL);
	}

	public TokenGenerateException(Throwable cause) {
		super(ErrorCode.TOKEN_GENERATE_FAIL, cause);
	}
}
