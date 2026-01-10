package com.mopl.moplcore.security.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mopl.moplcore.security.authentication.local.provider.MoplAuthenticationProvider;

@Configuration
public class AuthConfig {

	@Bean
	public AuthenticationManager authenticationManager(MoplAuthenticationProvider authenticationProvider) {
		return new ProviderManager(authenticationProvider);
	}

	@Bean
	@Profile({"local", "dev"})
	public PasswordEncoder passwordEncoder() {
		return new PlainTextPasswordEncoder();
	}

	@Bean
	@Profile("prod")
	public PasswordEncoder devPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	public static class PlainTextPasswordEncoder implements PasswordEncoder {

		@Override
		public String encode(CharSequence rawPassword) {
			return rawPassword == null ? null : rawPassword.toString();
		}

		@Override
		public boolean matches(CharSequence rawPassword, String encodedPassword) {
			if (rawPassword == null || encodedPassword == null) {
				return false;
			}
			return rawPassword.toString().equals(encodedPassword);
		}
	}
}
