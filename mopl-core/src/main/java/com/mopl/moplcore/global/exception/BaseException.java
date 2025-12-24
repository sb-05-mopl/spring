package com.mopl.moplcore.global.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;


public class BaseException extends RuntimeException {
	@Getter
	private final Instant timestamp;
	@Getter
	private final ErrorCode errorCode;
	private final Map<String, Object> details;

	public BaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.timestamp = Instant.now();
		this.errorCode = errorCode;
		this.details = new HashMap<>();
	}

	public BaseException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.timestamp = Instant.now();
		this.errorCode = errorCode;
		this.details = new HashMap<>();
	}

	public BaseException addDetail(String key, Object value) {
		this.details.put(key, value);
		return this;
	}

	public Map<String, Object> getDetails() { return Collections.unmodifiableMap(details); }
}