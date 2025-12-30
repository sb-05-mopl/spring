package com.mopl.moplcore.domain.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record ReviewUpdateRequest(
	String text,

	@DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다.")
	@DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
	Double rating
) {
}
