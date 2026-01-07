package com.mopl.moplcore.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider tokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String token = resolveToken(request);

    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!tokenProvider.validateAccessToken(token)) {
      SecurityContextHolder.clearContext();
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token");
      return;
    }

    UUID userId = tokenProvider.getUserId(token);

    UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

    var auth = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );

    SecurityContextHolder.getContext().setAuthentication(auth);
    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
