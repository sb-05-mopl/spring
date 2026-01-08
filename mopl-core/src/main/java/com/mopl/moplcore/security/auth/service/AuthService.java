package com.mopl.moplcore.security.auth.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.mopl.moplcore.security.auth.dto.JwtInformation;
import com.mopl.moplcore.security.exception.InValidAccessTokenException;
import com.mopl.moplcore.security.jwt.registry.JwtRegistry;
import com.mopl.moplcore.security.jwt.registry.JwtTokenProvider;
import com.mopl.moplcore.security.principal.MoplUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtRegistry jwtRegistry;
	private final JwtTokenProvider tokenProvider;
	private final UserDetailsService userDetailsService;

	public JwtInformation refreshToken(String refreshToken) {
		if (!tokenProvider.validateRefreshToken(refreshToken) || !jwtRegistry.hasActiveJwtInformationByRefreshToken(
			refreshToken)) {
			throw new InValidAccessTokenException();
		}

		String email = tokenProvider.getSubject(refreshToken);
		MoplUserDetails userDetails = (MoplUserDetails)userDetailsService.loadUserByUsername(email);

		String newAccessToken = tokenProvider.generateAccessToken(userDetails);
		String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

		JwtInformation newInfo = new JwtInformation(
			userDetails.getUserDto(),
			newAccessToken,
			newRefreshToken
		);

		jwtRegistry.rotateJwtInformation(refreshToken, newInfo);
		return newInfo;
	}
}
