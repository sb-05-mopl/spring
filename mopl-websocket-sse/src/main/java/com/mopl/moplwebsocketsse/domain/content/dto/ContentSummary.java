package com.mopl.moplwebsocketsse.domain.content.dto;

import java.util.List;
import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.content.entity.Tag;
import com.mopl.moplwebsocketsse.domain.content.entity.Type;

public record ContentSummary(
	UUID id,
	Type type,
	String title,
	String description,
	String thumbnailUrl,
	List<Tag> tags,
	double averageRating,
	int reviewCount
) {
}
