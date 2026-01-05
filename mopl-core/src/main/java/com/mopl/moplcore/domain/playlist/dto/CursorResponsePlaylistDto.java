package com.mopl.moplcore.domain.playlist.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CursorResponsePlaylistDto {
	private List<PlaylistDto> data;
	private String nextCursor;
	private UUID nextIdAfter;
	private boolean hasNext;
	private long totalCount;
	private String sortBy;
	private String sortDirection;
}