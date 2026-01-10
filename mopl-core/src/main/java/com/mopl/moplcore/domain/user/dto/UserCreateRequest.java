package com.mopl.moplcore.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreateRequest {

	@NotBlank
	String name;

	@NotBlank
	String email;

	@NotBlank
	String password;

}
