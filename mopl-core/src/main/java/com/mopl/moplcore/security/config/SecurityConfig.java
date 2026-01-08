package com.mopl.moplcore.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.mopl.moplcore.security.filter.JwtAuthenticationFilter;
import com.mopl.moplcore.security.handler.MoplLoginSuccessHandler;
import com.mopl.moplcore.security.handler.MoplLogoutSuccessHandler;
import com.mopl.moplcore.security.handler.SpaCsrfTokenRequestHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		MoplLoginSuccessHandler moplLoginSuccessHandler,
		MoplLogoutSuccessHandler moplLogoutSuccessHandler,
		JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(SecurityPaths.PUBLIC_PATHS).permitAll()
				.requestMatchers(HttpMethod.POST, SecurityPaths.MethodSpecific.POST_ONLY).permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(login -> login
				.loginProcessingUrl("/api/auth/sign-in")
				.usernameParameter("username")
				.passwordParameter("password")
				.successHandler(moplLoginSuccessHandler)
			)

			.logout(logout -> logout
				.logoutUrl("/api/auth/sign-out")
				.logoutSuccessHandler(moplLogoutSuccessHandler)
			)
			.csrf(csrf -> csrf
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)

			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
		;

		return http.build();
	}
}
