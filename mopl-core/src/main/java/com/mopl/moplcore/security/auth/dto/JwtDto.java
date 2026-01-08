package com.mopl.moplcore.security.auth.dto;

import com.mopl.moplcore.domain.user.dto.UserDto;

public record JwtDto(
    UserDto userDto,
    String accessToken
) {

}
