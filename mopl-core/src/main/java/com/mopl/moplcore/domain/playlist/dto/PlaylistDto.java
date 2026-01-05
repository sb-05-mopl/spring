package com.mopl.moplcore.domain.playlist.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.mopl.moplcore.domain.content.dto.ContentSummary;
import com.mopl.moplcore.domain.user.dto.UserSummary;
import lombok.Builder;

@Builder
public record PlaylistDto(
	UUID id,
	UserSummary owner,
	String title,
	String description,
	LocalDateTime updatedAt,
	Long subscriberCount,
	Boolean subscribedByMe,
	List<ContentSummary> contents
) {
}