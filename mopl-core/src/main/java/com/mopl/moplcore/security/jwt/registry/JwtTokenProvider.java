package com.mopl.moplcore.security.jwt.registry;

import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.Role;
import com.mopl.moplcore.security.principal.MoplUserDetails;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private final int accessTokenExpirationMs;
  private final int refreshTokenExpirationMs;

  public final JWSSigner accessTokenSigner;
  public final JWSVerifier accessTokenVerifier;

  public final JWSSigner refreshTokenSigner;
  public final JWSVerifier refreshTokenVerifier;

  public JwtTokenProvider(
      @Value("${jwt.access-token.secret}") String accessTokenSecret,
      @Value("${jwt.access-token.expiration-ms}") int accessTokenExpirationMs,
      @Value("${jwt.refresh-token.secret}") String refreshTokenSecret,
      @Value("${jwt.refresh-token.expiration-ms}") int refreshTokenExpirationMs
  ) throws JOSEException {
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;

    byte[] accessTokenSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
    this.accessTokenSigner = new MACSigner(accessTokenSecretBytes);
    this.accessTokenVerifier = new MACVerifier(accessTokenSecretBytes);

    byte[] refreshTokenSecretBytes = refreshTokenSecret.getBytes(StandardCharsets.UTF_8);
    this.refreshTokenSigner = new MACSigner(refreshTokenSecretBytes);
    this.refreshTokenVerifier = new MACVerifier(refreshTokenSecretBytes);
  }

  public String generateAccessToken(MoplUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, "access");
  }

  public String generateRefreshToken(MoplUserDetails userDetails) throws JOSEException {
    return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, "refresh");
  }

  public String generateToken(MoplUserDetails userDetails, int expirationMs, JWSSigner signer,
      String tokenType) throws JOSEException {

    String tokenId = UUID.randomUUID().toString();
    UserDto user = userDetails.getUserDto();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(user.email())
        .jwtID(tokenId)
        .claim("userId", user.id().toString())
        .claim("type", tokenType)
        .claim("role", user.role().name())
        .claim("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .issueTime(now)
        .expirationTime(expiryDate)
        .build();

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS256),
        claimsSet
    );

    signedJWT.sign(signer);
    String token = signedJWT.serialize();

    return token;
  }

  public boolean validateAccessToken(String token) {
    return validateToken(token, accessTokenVerifier, "access");
  }

  public boolean validateRefreshToken(String token) {
    return validateToken(token, refreshTokenVerifier, "refresh");
  }

  private boolean validateToken(String token, JWSVerifier verifier, String expectedType) {

    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      if (!signedJWT.verify(verifier)) {
        return false;
      }

      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      if (!expectedType.equals(tokenType)) {
        return false;
      }

      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      return expirationTime != null && !expirationTime.before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  public Cookie generateRefreshTokenCookie(String refreshToken) {
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(refreshTokenExpirationMs / 1000);
    return cookie;
  }

  public Cookie generateRefreshTokenExpirationCookie() {
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    return cookie;
  }


  public String getTokenId(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet().getJWTID();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  public UUID getUserId(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      String userIdStr = (String) signedJWT.getJWTClaimsSet().getClaim("userId");
      if (userIdStr == null) {
        throw new IllegalArgumentException("User ID claim not found in JWT token");
      }
      return UUID.fromString(userIdStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  public Role getRole(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      String roleStr = (String) signedJWT.getJWTClaimsSet().getClaim("role");
      if (roleStr == null) {
        throw new IllegalArgumentException("Role claim not found in JWT token");
      }
      return Role.valueOf(roleStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  public String getSubject(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet().getSubject();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }
}