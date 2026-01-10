package com.mopl.moplcore.security.authentication.jwt.token;

import lombok.Getter;

@Getter
public enum TokenType {
	ACCESS("access"),
	REFRESH("refresh");

	private final String value;

	TokenType(String value) {
		this.value = value;
	}
}
