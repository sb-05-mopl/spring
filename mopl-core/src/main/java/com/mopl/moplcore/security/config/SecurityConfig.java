package com.mopl.moplcore.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplcore.security.handler.LogoutSuccessHandler;
import com.mopl.moplcore.security.handler.SpaCsrfTokenRequestHandler;
import com.mopl.moplcore.security.filter.JwtAuthenticationFilter;
import com.mopl.moplcore.security.handler.LoginSuccessHandler;
import com.mopl.moplcore.security.jwt.registry.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      LoginSuccessHandler loginSuccessHandler,
	  LogoutSuccessHandler logoutSuccessHandler,
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
            .successHandler(loginSuccessHandler)
        )

        .logout(logout -> logout
            .logoutUrl("/api/auth/sign-out")
            .logoutSuccessHandler(logoutSuccessHandler)
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
