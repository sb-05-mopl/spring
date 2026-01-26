package com.mopl.moplcore.security.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.mopl.moplcore.security.authentication.jwt.handler.JwtAccessDeniedHandler;
import com.mopl.moplcore.security.authentication.jwt.handler.JwtAuthenticationEntryPoint;
import com.mopl.moplcore.security.authentication.jwt.filter.JwtAuthenticationFilter;
import com.mopl.moplcore.security.authentication.local.handler.MoplLoginSuccessHandler;
import com.mopl.moplcore.security.authentication.local.handler.MoplLogoutSuccessHandler;
import com.mopl.moplcore.security.authentication.oauth.handler.MoplOAuth2SuccessHandler;
import com.mopl.moplcore.security.authentication.oauth.handler.MoplOAuth2FailureHandler;
import com.mopl.moplcore.security.api.handler.SpaCsrfTokenRequestHandler;
import com.mopl.moplcore.security.authentication.oauth.service.MoplOAuth2UserService;
import com.mopl.moplcore.security.authentication.oauth.service.MoplOidcUserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		MoplLoginSuccessHandler moplLoginSuccessHandler,
		MoplLogoutSuccessHandler moplLogoutSuccessHandler,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		MoplOAuth2UserService oAuth2UserService,
		MoplOidcUserService oidcUserService,
		MoplOAuth2SuccessHandler moplOAuth2SuccessHandler,
		MoplOAuth2FailureHandler moplOAuth2FailureHandler,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
		JwtAccessDeniedHandler jwtAccessDeniedHandler
	) throws Exception {

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
			.exceptionHandling(e->e
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)
			.oauth2Login(
				login -> login
					.userInfoEndpoint(info -> info
						.userService(oAuth2UserService)
						.oidcUserService(oidcUserService)
					)
					.successHandler(moplOAuth2SuccessHandler)
					.failureHandler(moplOAuth2FailureHandler)
			)
			.addFilterAfter(jwtAuthenticationFilter, ExceptionTranslationFilter.class);
		;

		return http.build();
	}
}
