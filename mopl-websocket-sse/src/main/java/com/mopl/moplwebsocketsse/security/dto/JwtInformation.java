package com.mopl.moplwebsocketsse.security.dto;


import com.mopl.moplwebsocketsse.domain.user.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtInformation {

	private UserDto userDto;
	private String accessToken;
	private String refreshToken;

	public void rotate(String newAccessToken, String newRefreshToken) {
		this.accessToken = newAccessToken;
		this.refreshToken = newRefreshToken;
	}
}
