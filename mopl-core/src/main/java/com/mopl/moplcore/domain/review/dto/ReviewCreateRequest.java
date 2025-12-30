package com.mopl.moplcore.domain.review.dto;

import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
	@NotNull(message = "콘텐츠 ID는 필수입니다.")
	UUID contentId,

	@NotBlank(message = "리뷰 내용은 필수입니다.")
	String text,

	@NotNull(message = "평점은 필수입니다.")
	@DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다.")
	@DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
	Double rating
) {
}