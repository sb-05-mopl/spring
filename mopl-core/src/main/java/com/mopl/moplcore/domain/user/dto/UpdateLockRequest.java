package com.mopl.moplcore.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateLockRequest {
	boolean locked;
}
