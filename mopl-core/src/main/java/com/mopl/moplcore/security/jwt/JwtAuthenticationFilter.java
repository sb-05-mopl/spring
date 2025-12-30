package com.mopl.moplcore.security.jwt;

import com.mopl.moplcore.domain.user.entity.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;


  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    try {
      String header = request.getHeader("Authorization");
      if (header == null || !header.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
      }

      String token = header.substring("Bearer ".length());

      if (!jwtTokenProvider.validateAccessToken(token)) {
        filterChain.doFilter(request, response);
        return;
      }

      UUID userId = jwtTokenProvider.getUserId(token);
      Role role = jwtTokenProvider.getRole(token);

      var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
      var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);

    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}
