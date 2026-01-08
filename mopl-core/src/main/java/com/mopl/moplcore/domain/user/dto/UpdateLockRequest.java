package com.mopl.moplcore.domain.user.dto;

import com.mopl.moplcore.domain.user.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateLockRequest {
	boolean locked;
}
