package com.mopl.moplwebsocketsse.security.exception;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class InValidAccessTokenException extends BaseException {
	public InValidAccessTokenException() {
		super(ErrorCode.INVALID_ACCESS_TOKEN);
	}
}
