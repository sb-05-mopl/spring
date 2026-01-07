package com.mopl.moplwebsocketsse.security.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 로컬 테스트 통과용 임시 코드
public class MockAuthenticationFilter extends OncePerRequestFilter {

	public static final String HEADER_USER_ID = "X-USER-ID";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String userIdHeader = request.getHeader(HEADER_USER_ID);

		if (userIdHeader != null && !userIdHeader.isBlank()) {
			try {
				UUID userId = UUID.fromString(userIdHeader);

				var auth = new UsernamePasswordAuthenticationToken(
					userId,
					null,
					Collections.emptyList()
				);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (IllegalArgumentException ignored) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);

		SecurityContextHolder.clearContext();
	}
}
