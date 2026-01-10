package com.mopl.moplcore.domain.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.mopl.moplcore.domain.user.entity.AuthProvider;
import com.mopl.moplcore.domain.user.entity.Role;
import com.mopl.moplcore.domain.user.entity.User;

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
	AuthProvider provider;

	public static UserDto from(User user){
		UserDto userDto = new UserDto(user.getId(), user.getCreatedAt(), user.getEmail(), user.getName(),
			user.getProfileImageUrl(), user.getRole(), user.isLocked(), user.getProvider());
		return userDto;
	}
}
