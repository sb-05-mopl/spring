package com.mopl.moplcore.security.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordRequest {
	String email;
}
