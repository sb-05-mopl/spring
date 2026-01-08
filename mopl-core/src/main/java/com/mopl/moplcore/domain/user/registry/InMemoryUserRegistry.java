package com.mopl.moplcore.domain.user.registry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// 레디스 대용
@Profile({"dev", "local"})
@Component
@RequiredArgsConstructor
public class InMemoryUserRegistry implements UserRegistry {

	private final Map<UUID, String> data = new ConcurrentHashMap<>();
	private final PasswordEncoder passwordEncoder;

	@Override
	public String setTempPassword(UUID userId) {
		String originPassword = UUID.randomUUID().toString().substring(0, 8);
		String encode = passwordEncoder.encode(originPassword);
		data.put(userId, encode);

		return originPassword;
	}

	@Override
	public String getEncodedPassword(UUID userId) {
		return data.get(userId);
	}

	@Override
	public boolean existById(UUID userId) {
		return data.containsKey(userId);
	}

	@Override
	public void removeTempPassword(UUID userId) {
		data.remove(userId);
	}
}
