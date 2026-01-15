package com.mopl.moplcore.security.authentication.jwt.exception;

import org.springframework.security.core.AuthenticationException;

public class InValidAccessTokenException extends AuthenticationException {
	public InValidAccessTokenException(String message) {
		super(message);
	}
}
