package com.mopl.moplcore.global.config;

import com.mopl.moplcore.security.SpaCsrfTokenRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**"
            ).permitAll()
            .anyRequest().permitAll()
        )
        .formLogin(login -> login
            .loginProcessingUrl("/api/auth/sign-in")
            .usernameParameter("username")
            .passwordParameter("password")
            .successHandler((request, response, authentication) -> {
              response.setStatus(200);
              response.setContentType("application/json;charset=UTF-8");
              response.getWriter().write("{\\\"accessToken\\\":\\\"TODO\\\",\\\"userDto\\\":{}}");
            })
            .failureHandler((request, response, exception) -> {
              response.setStatus(401);
              response.setContentType("application/json;charset=UTF-8");
              response.getWriter().write("{\"exceptionName\":\"UNAUTHORIZED\",\"message\":\"Bad credentials\"}");
            })
        )

        .logout(logout -> logout
            .logoutUrl("/api/auth/sign-out")
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            .deleteCookies("REFRESH_TOKEN")
        )
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
    ;

    return http.build();
  }
}
