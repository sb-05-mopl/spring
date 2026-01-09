package com.mopl.moplwebsocketsse.domain.directMessage.dto;

import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DirectMessageSearchRequest(
	String cursor,
	UUID idAfter,
	@NotNull @Min(1) @Max(100) Integer limit,
	@NotNull SortDirection sortDirection
) {
}