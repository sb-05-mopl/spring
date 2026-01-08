package com.mopl.moplcore.security.filter;

import com.mopl.moplcore.security.exception.InValidAccessToken;
import com.mopl.moplcore.security.jwt.registry.JwtTokenProvider;
import com.mopl.moplcore.security.principal.MoplUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		String accessToken = request.getHeader("Authorization").substring(7);

		if (!tokenProvider.validateAccessToken(accessToken))
			throw new InValidAccessToken();

		String email = tokenProvider.getSubject(accessToken);
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
			null, userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
}