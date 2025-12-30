package com.mopl.moplcore.domain.review.dto;

import java.util.UUID;

import com.mopl.moplcore.domain.user.dto.UserSummary;

public record ReviewDto(
	UUID id,
	UUID contentId,
	UserSummary author,
	String text,
	Double rating
) {
}
