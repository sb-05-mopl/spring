package com.mopl.moplwebsocketsse.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.mopl.moplwebsocketsse.security.filter.JwtAuthenticationFilter;
import com.mopl.moplwebsocketsse.security.handler.JwtAccessDeniedHandler;
import com.mopl.moplwebsocketsse.security.handler.JwtAuthenticationEntryPoint;
import com.mopl.moplwebsocketsse.security.handler.SpaCsrfTokenRequestHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class SecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
		JwtAccessDeniedHandler jwtAccessDeniedHandler,
		JwtAuthenticationFilter jwtAuthenticationFilter
	) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(SecurityPaths.PUBLIC_PATHS).permitAll()
				.anyRequest().authenticated()
			)
			.csrf(csrf -> csrf
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.exceptionHandling(e->e
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)
			.addFilterAfter(jwtAuthenticationFilter, ExceptionTranslationFilter.class);

		return http.build();
	}
}