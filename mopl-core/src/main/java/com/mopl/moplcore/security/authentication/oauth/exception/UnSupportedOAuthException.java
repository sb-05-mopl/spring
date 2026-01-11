package com.mopl.moplcore.security.authentication.oauth.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class UnSupportedOAuthException extends BaseException {
	public UnSupportedOAuthException() {
		super(ErrorCode.INVALID_ACCESS_TOKEN);
	}

	public static UnSupportedOAuthException withProvider(String provider) {
		UnSupportedOAuthException exception = new UnSupportedOAuthException();
		exception.addDetail("provider", provider);
		return exception;
	}

}
