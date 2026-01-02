package com.mopl.moplwebsocketsse.domain.auth.dto;

import com.mopl.moplwebsocketsse.domain.user.dto.UserDto;

public record JwtDto(
	UserDto userDto,
	String accessToken
) {

}
