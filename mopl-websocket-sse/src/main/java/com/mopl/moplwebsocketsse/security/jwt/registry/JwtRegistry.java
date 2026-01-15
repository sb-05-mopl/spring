package com.mopl.moplwebsocketsse.security.jwt.registry;

public interface JwtRegistry {
	boolean hasActiveJwtInformationByAccessToken(String accessToken);
}
