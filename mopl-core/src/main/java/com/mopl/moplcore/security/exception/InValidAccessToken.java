package com.mopl.moplcore.security.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class InValidAccessToken extends BaseException {
	public InValidAccessToken() {
		super(ErrorCode.INVALID_ACCESS_TOKEN);
	}
}
