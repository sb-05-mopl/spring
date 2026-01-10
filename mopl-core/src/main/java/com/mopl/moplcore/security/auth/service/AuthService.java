package com.mopl.moplcore.security.auth.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.user.entity.AuthProvider;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.event.SendEmailPasswordEvent;
import com.mopl.moplcore.domain.user.exception.AuthPasswordException;
import com.mopl.moplcore.domain.user.exception.UserNotFoundException;
import com.mopl.moplcore.domain.user.registry.UserRegistry;
import com.mopl.moplcore.domain.user.repository.UserRepository;
import com.mopl.moplcore.security.auth.dto.JwtInformation;
import com.mopl.moplcore.security.exception.InValidAccessTokenException;
import com.mopl.moplcore.security.jwt.JwtTokenProvider;
import com.mopl.moplcore.security.jwt.registry.JwtRegistry;
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
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserRegistry userRegistry;

	private final ApplicationEventPublisher publisher;

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

	@Transactional
	public void resetPassword(String toEmail) {
		User user = userRepository.findByEmail(toEmail).orElseThrow(() -> UserNotFoundException.withEmail(toEmail));
		if(user.getProvider() == AuthProvider.LOCAL){
			throw new AuthPasswordException();
		}

		user.updatePassword(passwordEncoder.encode(UUID.randomUUID().toString().substring(0, 10)));
		String tempPassword = userRegistry.setTempPassword(user.getId());

		SendEmailPasswordEvent event = new SendEmailPasswordEvent(toEmail, tempPassword, Instant.now());
		publisher.publishEvent(event);
	}
}
