package com.mopl.moplcore.domain.content.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CursorResponseContentDto {

	private List<ContentDto> data;
	private String nextCursor;
	private UUID nextIdAfter;
	private boolean hasNext;
	private long totalCount;
	private String sortBy;
	private String sortDirection;
}