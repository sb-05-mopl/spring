package com.mopl.moplcore.security.filter;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mopl.moplcore.security.exception.InValidCredentialException;
import com.mopl.moplcore.security.exception.LockedUserAccessException;
import com.mopl.moplcore.security.principal.MoplUserDetails;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MoplAuthenticationProvider implements AuthenticationProvider {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String email = authentication.getName();
		String password = authentication.getCredentials().toString();

		MoplUserDetails userDetails = (MoplUserDetails)userDetailsService.loadUserByUsername(email);

		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			throw new InValidCredentialException();
		}

		if(!userDetails.isEnabled()){
			throw new LockedUserAccessException();
		}

		userDetails.setPasswordEnable();

		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
