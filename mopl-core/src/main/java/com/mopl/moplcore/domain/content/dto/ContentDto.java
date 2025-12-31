package com.mopl.moplcore.domain.content.dto;

import java.util.List;
import java.util.UUID;

import com.mopl.moplcore.domain.content.entity.Type;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentDto {
	private UUID id;
	private Type type;
	private String title;
	private String description;
	private String thumbnailUrl;
	private List<String> tags;
	private double averageRating;
	private int reviewCount;
	private long watcherCount;
}