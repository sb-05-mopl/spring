package com.mopl.moplcore.domain.auth.controller;

import com.mopl.moplcore.domain.auth.dto.JwtDto;
import com.mopl.moplcore.domain.auth.dto.JwtInformation;
import com.mopl.moplcore.domain.auth.service.AuthService;
import com.mopl.moplcore.domain.user.service.UserService;
import com.mopl.moplcore.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    csrfToken.getToken();
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(@CookieValue("REFRESH_TOKEN") String refreshToken,
      HttpServletResponse response) {

    JwtInformation jwtInformation = authService.refreshToken(refreshToken);

    Cookie refreshCookie = jwtTokenProvider.generateRefreshTokenCookie(
        jwtInformation.getRefreshToken());
    response.addCookie(refreshCookie);

    JwtDto body = new JwtDto(
        jwtInformation.getUserDto(),
        jwtInformation.getAccessToken()
    );
    return ResponseEntity.status(HttpStatus.OK).body(body);
  }
}
