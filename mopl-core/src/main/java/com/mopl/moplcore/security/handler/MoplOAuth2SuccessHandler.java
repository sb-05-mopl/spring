package com.mopl.moplcore.security.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.mopl.moplcore.security.auth.dto.JwtInformation;
import com.mopl.moplcore.security.exception.UnexpectedPrincipalException;
import com.mopl.moplcore.security.jwt.JwtTokenProvider;
import com.mopl.moplcore.security.jwt.registry.JwtRegistry;
import com.mopl.moplcore.security.principal.MoplUserDetails;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoplOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider tokenProvider;
	private final JwtRegistry jwtRegistry;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException {

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof MoplUserDetails)) {
			throw new UnexpectedPrincipalException();
		}

		MoplUserDetails userDetails = (MoplUserDetails) principal;

		String accessToken = tokenProvider.generateAccessToken(userDetails);
		String refreshToken = tokenProvider.generateRefreshToken(userDetails);

		JwtInformation info = new JwtInformation(userDetails.getUserDto(), accessToken, refreshToken);

		jwtRegistry.registerJwtInformation(info);

		Cookie refreshCookie = tokenProvider.generateRefreshTokenCookie(refreshToken);
		response.addCookie(refreshCookie);
		response.sendRedirect("/");

	}
}
