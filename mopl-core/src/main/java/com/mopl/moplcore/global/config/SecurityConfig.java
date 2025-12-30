package com.mopl.moplcore.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplcore.security.SpaCsrfTokenRequestHandler;
import com.mopl.moplcore.security.jwt.JwtAuthenticationFilter;
import com.mopl.moplcore.security.jwt.JwtLoginSuccessHandler;
import com.mopl.moplcore.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtLoginSuccessHandler jwtLoginSuccessHandler,
      JwtTokenProvider jwtTokenProvider,
      ObjectMapper objectMapper
  ) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(HttpStatus.UNAUTHORIZED.value());
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.setCharacterEncoding("UTF-8");

              objectMapper.writeValue(
                  response.getOutputStream(),
                  java.util.Map.of("exceptionName", "UNAUTHORIZED", "message",
                      "Authentication required")
              );
            }))

        .formLogin(login -> login
            .loginProcessingUrl("/api/auth/sign-in")
            .usernameParameter("username")
            .passwordParameter("password")
            .successHandler(jwtLoginSuccessHandler)
            .failureHandler((request, response, exception) -> {
              response.setStatus(401);
              response.setContentType("application/json;charset=UTF-8");
              response.getWriter()
                  .write("{\"exceptionName\":\"UNAUTHORIZED\",\"message\":\"Bad credentials\"}");
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

        .addFilterBefore(
            new JwtAuthenticationFilter(jwtTokenProvider),
            UsernamePasswordAuthenticationFilter.class
        )
    ;

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
