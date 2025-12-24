package com.mopl.moplwebsocketsse.global.log;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {

	// MDC Keys
	public static final String MDC_REQUEST_ID = "requestId";
	public static final String MDC_REQUEST_URI = "requestUri";
	public static final String MDC_REQUEST_METHOD = "requestMethod";
	public static final String MDC_CLIENT_IP = "clientIp";

	// Header
	public static final String REQUEST_ID_HEADER = "X-Request-ID";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

		String requestId = resolveRequestId(request);

		MDC.put(MDC_REQUEST_ID, requestId);
		MDC.put(MDC_REQUEST_URI, request.getRequestURI());
		MDC.put(MDC_REQUEST_METHOD, request.getMethod());
		MDC.put(MDC_CLIENT_IP, extractClientIp(request));

		response.setHeader(REQUEST_ID_HEADER, requestId);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		MDC.clear();
	}

	private String resolveRequestId(HttpServletRequest request) {
		String incoming = request.getHeader(REQUEST_ID_HEADER);

		if (incoming != null && !incoming.isBlank() && incoming.length() <= 64) {
			return incoming.trim();
		}

		return UUID.randomUUID().toString().replace("-", "");
	}

	private String extractClientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return Optional.ofNullable(request.getRemoteAddr()).orElse("unknown");
	}
}
