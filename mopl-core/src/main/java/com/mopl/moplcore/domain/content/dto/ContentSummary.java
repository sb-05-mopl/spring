package com.mopl.moplcore.domain.content.dto;

import java.util.List;
import java.util.UUID;

import com.mopl.moplcore.domain.content.entity.Type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "콘텐츠 요약 정보")
@Builder
public record ContentSummary(
	UUID id,
	Type type,
	String title,
	String description,
	String thumbnailUrl,
	List<String> tags,
	Double averageRating,
	Integer reviewCount
) {
}