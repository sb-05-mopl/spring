package com.mopl.moplwebsocketsse.security.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mopl.moplwebsocketsse.security.config.SecurityPaths;
import com.mopl.moplwebsocketsse.security.exception.InValidAccessTokenException;
import com.mopl.moplwebsocketsse.security.jwt.JwtTokenProvider;
import com.mopl.moplwebsocketsse.security.jwt.registry.JwtRegistry;
import com.mopl.moplwebsocketsse.security.principal.MoplUserDetails;
import com.mopl.moplwebsocketsse.security.registry.JwtRegistry;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final int BEARER_PREFIX_LENGTH = 7;

	private final JwtTokenProvider tokenProvider;
	private final UserDetailsService userDetailsService;
	private final JwtRegistry jwtRegistry;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		String method = request.getMethod();

		return SecurityPaths.isPublicPath(path, method);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String path = request.getRequestURI();
		String method = request.getMethod();
		log.info("=== shouldNotFilter Debug ===");
		log.info("Request URI: {}", path);
		log.info("Method: {}", method);
		String accessToken = request.getHeader(AUTHORIZATION_HEADER).substring(BEARER_PREFIX_LENGTH);

		if (!tokenProvider.validateAccessToken(accessToken)) {
			throw new InValidAccessTokenException("Invalid access token");
		}

		if (!jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken)) {
			throw new InValidAccessTokenException("Inactive access token");
		}
		String email = tokenProvider.getSubject(accessToken);
		MoplUserDetails userDetails = (MoplUserDetails)userDetailsService.loadUserByUsername(email);

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
			null, userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
}