package com.mopl.moplcore.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String accessToken = authHeader.substring(7);

    try {
      if (!jwtTokenProvider.validateAccessToken(accessToken)) {
        filterChain.doFilter(request, response);
        return;
      }

      if (!jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      UUID userId = jwtTokenProvider.getUserId(accessToken);
      var role = jwtTokenProvider.getRole(accessToken);

      var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
      var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);

    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
    }
  }
}