package com.mopl.moplwebsocketsse.domain.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.user.entity.Role;

public record UserDto(
	UUID id,
	Instant createdAt,
	String email,
	String name,
	String profileImageUrl,
	Role role,
	boolean locked
) {

}
