package com.mopl.moplcore.security.auth.dto;

import com.mopl.moplcore.domain.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtDto {
	UserDto userDto;
	String accessToken;
}

