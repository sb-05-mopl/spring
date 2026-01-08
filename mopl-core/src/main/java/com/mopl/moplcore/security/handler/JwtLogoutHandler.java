package com.mopl.moplcore.security.handler;

import com.mopl.moplcore.security.jwt.registry.JwtRegistry;
import com.mopl.moplcore.security.jwt.registry.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider tokenProvider;
  private final JwtRegistry jwtRegistry;


  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    Cookie refreshTokenExpirationCookie = tokenProvider.generateRefreshTokenExpirationCookie();

    response.addCookie(refreshTokenExpirationCookie);

    Cookie[] cookies = request.getCookies();
    if (cookies == null) return;

    Arrays.stream(cookies)
        .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
        .findFirst()
        .ifPresent(cookie -> {
          String refreshToken = cookie.getValue();
          UUID userId = tokenProvider.getUserId(refreshToken);
          jwtRegistry.invalidateJwtInformationByUserId(userId);
        });
  }
}
