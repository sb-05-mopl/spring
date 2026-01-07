package com.mopl.moplwebsocketsse.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.user.entity.Role;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

@Component
public class JwtTokenProvider {

	private final JWSVerifier accessTokenVerifier;

	public JwtTokenProvider(
		@Value("${jwt.access-token.secret}") String accessTokenSecret
	) throws JOSEException {
		byte[] secretBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
		this.accessTokenVerifier = new MACVerifier(secretBytes);
	}

	public boolean validateAccessToken(String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);

			if (!signedJWT.verify(accessTokenVerifier)) {
				return false;
			}

			String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
			if (!"access".equals(tokenType)) {
				return false;
			}

			Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
			return expirationTime != null && !expirationTime.before(new Date());
		} catch (Exception e) {
			return false;
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
}