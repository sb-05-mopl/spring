package com.mopl.moplcore.domain.auth.service;

import com.mopl.moplcore.domain.auth.dto.JwtInformation;
import com.mopl.moplcore.security.MoplUserDetails;
import com.mopl.moplcore.security.jwt.JwtRegistry;
import com.mopl.moplcore.security.jwt.JwtTokenProvider;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtRegistry jwtRegistry;
  private final JwtTokenProvider tokenProvider;
  private final UserDetailsService userDetailsService;

  public JwtInformation refreshToken(String refreshToken) {
    if (!tokenProvider.validateRefreshToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
          "Invalid or expired refresh token");
    }

    String email = tokenProvider.getSubject(refreshToken);
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    if (!(userDetails instanceof MoplUserDetails moplUserDetails)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user details");
    }

    try {
      String newAccessToken = tokenProvider.generateAccessToken(moplUserDetails);
      String newRefreshToken = tokenProvider.generateRefreshToken(moplUserDetails);

      JwtInformation newInfo = new JwtInformation(
          moplUserDetails.getUserDto(),
          newAccessToken,
          newRefreshToken
      );

      jwtRegistry.rotateJwtInformation(refreshToken, newInfo);
      return newInfo;

    } catch (JOSEException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to generate token", e);
    }
  }
}
