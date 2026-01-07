package com.mopl.moplcore.domain.content.dto;

import java.util.List;

import lombok.Getter;

public record ContentUpdateRequest(
	String title,
	String description,
	String thumbnailUrl,
	List<String> tags
) {
}