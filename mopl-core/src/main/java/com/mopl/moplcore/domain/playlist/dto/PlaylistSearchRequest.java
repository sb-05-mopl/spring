package com.mopl.moplcore.domain.playlist.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistSearchRequest {
	private String keywordLike;
	private UUID ownerIdEqual;
	private UUID subscriberIdEqual;
	private String cursor;
	private UUID idAfter;

	@NotNull(message = "limit은 필수입니다")
	@Min(value = 1, message = "limit은 최소 1 이상이어야 합니다")
	@Max(value = 100, message = "limit은 최대 100 이하이어야 합니다")
	private Integer limit;

	@NotNull(message = "sortDirection은 필수입니다")
	private SortDirection sortDirection;

	@NotNull(message = "sortBy는 필수입니다")
	private PlaylistSortBy sortBy;

	public enum PlaylistSortBy {
		updatedAt,
		subscribeCount
	}

	public enum SortDirection {
		ASCENDING,
		DESCENDING
	}
}