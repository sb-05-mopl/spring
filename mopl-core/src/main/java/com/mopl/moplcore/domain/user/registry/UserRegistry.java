package com.mopl.moplcore.domain.user.registry;

import java.util.UUID;

public interface UserRegistry {
	String setTempPassword(UUID userId);
	String getEncodedPassword(UUID userId);
	boolean existById(UUID userId);
	void removeTempPassword(UUID userId);
}
