package com.mopl.moplcore.domain.user.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class AuthPasswordException extends BaseException {
	public AuthPasswordException() {
		super(ErrorCode.AUTH_PASSWORD_CHANGE);
	}
}
