package com.mopl.mopl_spring.global.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
	Instant timestamp,
	String code,
	String message,
	Map<String, Object> details,
	String exceptionType,
	int status
) {
	public ErrorResponse(BaseException exception) {
		this(
			exception.getTimestamp(),
			exception.getErrorCode().name(),
			exception.getMessage(),
			exception.getDetails(),
			exception.getClass().getSimpleName(),
			exception.getErrorCode().getHttpStatus().value()
		);
	}

	public ErrorResponse(Exception exception, int status) {
		this(
			Instant.now(),
			exception.getClass().getSimpleName(),
			exception.getMessage(),
			Map.of(),
			exception.getClass().getSimpleName(),
			status
		);
	}
}