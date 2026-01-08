package com.mopl.moplcore.security.jwt.registry;

import java.util.UUID;

import com.mopl.moplcore.security.auth.dto.JwtInformation;

public interface JwtRegistry {

	void registerJwtInformation(JwtInformation jwtInformation);

	void invalidateJwtInformationByUserId(UUID userId);

	boolean hasActiveJwtInformationByUserId(UUID userId);

	boolean hasActiveJwtInformationByAccessToken(String accessToken);

	boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

	void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation);

	void clearExpiredJwtInformation();

}
