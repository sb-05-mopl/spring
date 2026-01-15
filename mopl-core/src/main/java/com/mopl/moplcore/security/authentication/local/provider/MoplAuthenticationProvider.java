package com.mopl.moplcore.security.authentication.local.provider;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mopl.moplcore.domain.user.registry.UserRegistry;
import com.mopl.moplcore.security.authentication.local.exception.InValidCredentialException;
import com.mopl.moplcore.security.authentication.local.exception.LockedUserAccessException;
import com.mopl.moplcore.security.core.principal.MoplUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoplAuthenticationProvider implements AuthenticationProvider {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final UserRegistry userRegistry;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String email = authentication.getName();
		String password = authentication.getCredentials().toString();

		MoplUserDetails userDetails = (MoplUserDetails)userDetailsService.loadUserByUsername(email);
		UUID id = userDetails.getUserDto().getId();

		if (!userDetails.isEnabled()) {
			throw new LockedUserAccessException("Locked User");
		}

		if (userRegistry.existById(id)) {
			if (!passwordEncoder.matches(password, userRegistry.getEncodedPassword(id))) {
				throw new InValidCredentialException("Password Not Matched-temp");
			}
			userRegistry.removeTempPassword(id);
		} else {
			if (!passwordEncoder.matches(password, userDetails.getPassword())) {
				throw new InValidCredentialException("Password Not Matched-db");
			}
		}

		userDetails.setPasswordEnable();

		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
