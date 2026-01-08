package com.mopl.moplcore.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplcore.security.handler.SpaCsrfTokenRequestHandler;
import com.mopl.moplcore.security.filter.JwtAuthenticationFilter;
import com.mopl.moplcore.security.handler.LoginSuccessHandler;
import com.mopl.moplcore.security.jwt.registry.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
      JwtTokenProvider jwtTokenProvider,
      ObjectMapper objectMapper,
      JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index.html", "/*.html", "/favicon.ico", "/assets/**").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers("/uploads/**").permitAll()
            .requestMatchers("/**").permitAll()
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
            .successHandler(loginSuccessHandler)
            .failureHandler((request, response, exception) -> {
              response.setStatus(401);
              response.setContentType("application/json;charset=UTF-8");
              response.getWriter()
                  .write("{\"exceptionName\":\"UNAUTHORIZED\",\"message\":\"Bad credentials\"}");
            })
        )

        .logout(logout -> logout
            .logoutUrl("/api/auth/sign-out")
            .addLogoutHandler(jwtLogoutHandler)
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
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

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
