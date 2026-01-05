package com.mopl.moplcore.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplcore.domain.auth.dto.JwtDto;
import com.mopl.moplcore.domain.auth.dto.JwtInformation;
import com.mopl.moplcore.global.exception.ErrorResponse;
import com.mopl.moplcore.security.MoplUserDetails;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider tokenProvider;
  private final JwtRegistry jwtRegistry;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    if (authentication.getPrincipal() instanceof MoplUserDetails userDetails) {
      try {
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        Cookie refreshCookie = tokenProvider.generateRefreshTokenCookie(refreshToken);
        response.addCookie(refreshCookie);

        JwtDto jwtDto = new JwtDto(
            userDetails.getUserDto(),
            accessToken
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

        jwtRegistry.registerJwtInformation(
            new JwtInformation(
                userDetails.getUserDto(),
                accessToken,
                refreshToken
            )
        );
      } catch (JOSEException e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ErrorResponse errorResponse = new ErrorResponse(
            new RuntimeException("Token generation failed"),
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        );
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      }
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      ErrorResponse errorResponse = new ErrorResponse(
          new RuntimeException("Authentication failed: Invalid user details"),
          HttpServletResponse.SC_UNAUTHORIZED
      );
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }
}