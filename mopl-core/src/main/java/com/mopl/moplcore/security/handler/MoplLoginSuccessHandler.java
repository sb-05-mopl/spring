package com.mopl.moplcore.security.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplcore.security.auth.dto.JwtDto;
import com.mopl.moplcore.security.auth.dto.JwtInformation;
import com.mopl.moplcore.security.exception.UnexpectedPrincipalException;
import com.mopl.moplcore.security.jwt.JwtTokenProvider;
import com.mopl.moplcore.security.jwt.registry.JwtRegistry;
import com.mopl.moplcore.security.principal.MoplUserDetails;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MoplLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider tokenProvider;
	private final JwtRegistry jwtRegistry;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication
	) throws IOException {
		if (!(authentication.getPrincipal() instanceof MoplUserDetails userDetails)) {
			throw new UnexpectedPrincipalException();
		}

		String accessToken = tokenProvider.generateAccessToken(userDetails);
		String refreshToken = tokenProvider.generateRefreshToken(userDetails);
		JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), accessToken);
		JwtInformation info = new JwtInformation(userDetails.getUserDto(), accessToken, refreshToken);
		jwtRegistry.registerJwtInformation(info);

		Cookie refreshCookie = tokenProvider.generateRefreshTokenCookie(refreshToken);
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.addCookie(refreshCookie);
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
	}
}