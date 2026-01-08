package com.mopl.moplcore.security.jwt.registry;

import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.Role;
import com.mopl.moplcore.global.exception.ErrorResponse;
import com.mopl.moplcore.security.exception.TokenGenerateException;
import com.mopl.moplcore.security.jwt.token.TokenType;
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
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
    private static final int SECONDS_IN_MS = 1000;

    private final int accessTokenExpirationMs;
    private final int refreshTokenExpirationMs;

    private final JWSSigner accessTokenSigner;
    private final JWSVerifier accessTokenVerifier;

    private final JWSSigner refreshTokenSigner;
    private final JWSVerifier refreshTokenVerifier;

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

    public String generateAccessToken(MoplUserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpirationMs, accessTokenSigner, TokenType.ACCESS);
    }

    public String generateRefreshToken(MoplUserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpirationMs, refreshTokenSigner, TokenType.REFRESH);
    }

    private String generateToken(MoplUserDetails userDetails, int expirationMs, JWSSigner signer, TokenType tokenType) {
        try {
            String tokenId = UUID.randomUUID().toString();
            UserDto user = userDetails.getUserDto();

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expirationMs);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .jwtID(tokenId)
                    .claim("userId", user.getId().toString())
                    .claim("type", tokenType.getValue())
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
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new TokenGenerateException(e);
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, accessTokenVerifier, TokenType.ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshTokenVerifier, TokenType.REFRESH);
    }

    private boolean validateToken(String token, JWSVerifier verifier, TokenType expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            if (!expectedType.getValue().equals(tokenType)) {
                return false;
            }

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null && !expirationTime.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Cookie generateRefreshTokenCookie(String refreshToken) {
        return createRefreshCookie(refreshToken, refreshTokenExpirationMs / SECONDS_IN_MS);
    }

    public Cookie generateRefreshTokenExpirationCookie() {
        return createRefreshCookie("", 0);
    }

    private Cookie createRefreshCookie(String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }


    public String getTokenId(String token) {
        return getClaim(token, JWTClaimsSet::getJWTID);
    }

    public UUID getUserId(String token) {
        String userIdStr = getClaim(token, claims -> (String) claims.getClaim("userId"));
        if (userIdStr == null) {
            throw new IllegalArgumentException("User ID claim not found in JWT token");
        }
        return UUID.fromString(userIdStr);
    }

    public Role getRole(String token) {
        String roleStr = getClaim(token, claims -> (String) claims.getClaim("role"));
        if (roleStr == null) {
            throw new IllegalArgumentException("Role claim not found in JWT token");
        }
        return Role.valueOf(roleStr);
    }

    public String getSubject(String token) {
        return getClaim(token, JWTClaimsSet::getSubject);
    }

    private <T> T getClaim(String token, ClaimExtractor<T> extractor) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return extractor.extract(claims);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    @FunctionalInterface
    private interface ClaimExtractor<T> {
        T extract(JWTClaimsSet claims) throws ParseException;
    }
}