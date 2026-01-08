package com.mopl.moplcore.security.handler;

import com.mopl.moplcore.security.jwt.registry.JwtRegistry;
import com.mopl.moplcore.security.jwt.registry.JwtTokenProvider;
import com.mopl.moplcore.security.principal.MoplUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogoutHandler implements LogoutSuccessHandler {

	private final JwtTokenProvider tokenProvider;
	private final JwtRegistry jwtRegistry;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) {
		Cookie cookie = tokenProvider.generateRefreshTokenExpirationCookie();
		MoplUserDetails userDetails = (MoplUserDetails)authentication.getPrincipal();

		jwtRegistry.invalidateJwtInformationByUserId(userDetails.getUserDto().getId());
		response.addCookie(cookie);

	}
}
