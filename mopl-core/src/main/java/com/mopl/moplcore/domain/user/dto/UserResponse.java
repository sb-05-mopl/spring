package com.mopl.moplcore.domain.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.mopl.moplcore.domain.user.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
	UUID id;
	Instant createdAt;
	String email;
	String name;
	String profileImageUrl;
	Role role;
	boolean locked;
}
