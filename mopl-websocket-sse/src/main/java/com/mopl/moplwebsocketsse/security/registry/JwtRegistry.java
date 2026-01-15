package com.mopl.moplwebsocketsse.security.registry;

import java.util.UUID;

import com.mopl.moplwebsocketsse.security.dto.JwtInformation;

public interface JwtRegistry {

	void registerJwtInformation(JwtInformation jwtInformation);

	void invalidateJwtInformationByUserId(UUID userId);

	boolean hasActiveJwtInformationByUserId(UUID userId);

	boolean hasActiveJwtInformationByAccessToken(String accessToken);

	boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

	void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation);

	void clearExpiredJwtInformation();

}
