package com.mopl.moplcore.security.authentication.oauth.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MoplOAuth2FailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException exception
	) throws IOException {
		log.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);
		response.sendRedirect(resolveRedirectUrl(request, "/login?error"));
	}

	private String resolveRedirectUrl(HttpServletRequest request, String path) {
		String proto = request.getHeader("X-Forwarded-Proto");
		String host = request.getHeader("X-Forwarded-Host");
		if (proto == null) {
			proto = request.getScheme();
		}
		if (host == null) {
			host = request.getHeader("Host");
		}
		if (host == null) {
			host = request.getServerName();
		}
		return proto + "://" + host + path;
	}
}
