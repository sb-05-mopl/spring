package com.mopl.moplcore.security.authentication.local.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class InValidCredentialException extends BaseException {
	public InValidCredentialException() {
		super(ErrorCode.INVALID_CREDENTIALS);
	}
}
