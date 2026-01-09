package com.mopl.moplwebsocketsse.security.exception;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class InValidRefreshTokenException extends BaseException {
	public InValidRefreshTokenException() {
		super(ErrorCode.INVALID_REFRESH_TOKEN);
	}
}
