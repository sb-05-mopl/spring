package com.mopl.moplwebsocketsse.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.user.entity.Role;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private final int accessTokenExpirationMs;

  public final JWSSigner accessTokenSigner;
  public final JWSVerifier accessTokenVerifier;

  public JwtTokenProvider(
      @Value("${jwt.access-token.secret}") String accessTokenSecret,
      @Value("${jwt.access-token.expiration-ms}") long accessTokenExpirationMs
  ) {
    this.accessTokenExpirationMs = (int) accessTokenExpirationMs;

    byte[] accessTokenSecretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
    try {
      this.accessTokenSigner = new MACSigner(accessTokenSecretBytes);
      this.accessTokenVerifier = new MACVerifier(accessTokenSecretBytes);
    } catch (JOSEException e) {
      throw new RuntimeException("JWT 서명/검증 키 초기화에 실패했습니다.", e);
    }
  }

  public String generateAccessToken(UUID userId, Role role) {

    try {
      return generateToken(userId, role, accessTokenExpirationMs, accessTokenSigner, "access");
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 서명에 실패했습니다." + e.getMessage(), e);
    }
  }

  public String generateToken(UUID userId, Role role, int expirationMs, JWSSigner signer,
      String tokenType) throws JOSEException {

    String tokenId = UUID.randomUUID().toString();
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(userId.toString())
        .jwtID(tokenId)
        .claim("userId", userId.toString())
        .claim("type", tokenType)
        .claim("role", role.name())
        .claim("roles", List.of(role.name()))
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

  private boolean validateToken(String token, JWSVerifier verifier, String expectedType) {

    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      if (!signedJWT.verify(verifier)) {
        return false;
      }

      String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
      if (!tokenType.equals(expectedType)) {
        return false;
      }

      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expirationTime == null || expirationTime.before(new Date())) {
        return false;
      }

      return true;
    } catch (Exception e) {
      return false;
    }
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
        userIdStr = signedJWT.getJWTClaimsSet().getSubject();
      }
      if (userIdStr == null) {
        throw new IllegalArgumentException("User Id claim not found in JWT token");
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