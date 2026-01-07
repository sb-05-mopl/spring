package com.mopl.moplcore.domain.content.dto;

import java.util.List;

import com.mopl.moplcore.domain.content.entity.Type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public record ContentCreateRequest(
	@NotNull(message = "타입은 필수입니다")
	Type type,

	@NotBlank(message = "제목은 필수입니다")
	String title,

	@NotBlank(message = "설명은 필수입니다")
	String description,

	@NotNull(message = "sourceId는 필수입니다")
	Long sourceId,

	String thumbnailUrl,

	@NotNull(message = "태그는 필수입니다")
	List<String> tags
) {
}