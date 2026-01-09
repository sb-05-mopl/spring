package com.mopl.moplwebsocketsse.domain.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.user.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
	UUID id;
	Instant createdAt;
	String email;
	String name;
	String profileImageUrl;
	Role role;
	boolean locked;
}
