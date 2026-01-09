package com.mopl.moplwebsocketsse.security.exception;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class InValidCredentialException extends BaseException {
	public InValidCredentialException() {
		super(ErrorCode.INVALID_CREDENTIALS);
	}
}
