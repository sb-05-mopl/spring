package com.mopl.moplcore.security.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class InvalidCredentialException extends BaseException {
	public InvalidCredentialException() {
		super(ErrorCode.INVALID_CREDENTIALS);
	}
}
