package com.mopl.moplcore.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.moplcore.domain.auth.dto.JwtDto;
import com.mopl.moplcore.security.MoplUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;


  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {

    MoplUserDetails userDetails = (MoplUserDetails) authentication.getPrincipal();

    String accessToken = jwtTokenProvider.generateAccessToken(
        userDetails.getUserDto().id(),
        userDetails.getUserDto().role()
    );

    JwtDto body = new JwtDto(userDetails.getUserDto(), accessToken);

    response.setStatus(HttpStatus.OK.value());
    response.setContentType("application/json;charset=UTF-8");
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}