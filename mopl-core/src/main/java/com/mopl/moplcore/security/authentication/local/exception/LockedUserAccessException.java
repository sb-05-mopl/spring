package com.mopl.moplcore.security.authentication.local.exception;

import org.springframework.security.core.AuthenticationException;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class LockedUserAccessException extends AuthenticationException {
	public LockedUserAccessException(String message) {
		super(message);
	}
}
