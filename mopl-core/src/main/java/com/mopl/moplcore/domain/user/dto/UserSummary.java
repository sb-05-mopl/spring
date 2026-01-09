package com.mopl.moplcore.domain.user.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSummary {
	UUID userId;
	String name;
	String profileImageUrl;
}