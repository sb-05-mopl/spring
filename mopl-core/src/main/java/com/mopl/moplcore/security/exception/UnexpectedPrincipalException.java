package com.mopl.moplcore.security.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class UnexpectedPrincipalException extends BaseException {
	public UnexpectedPrincipalException() {
		super(ErrorCode.UNEXPECTED_PRINCIPAL);
	}
}
