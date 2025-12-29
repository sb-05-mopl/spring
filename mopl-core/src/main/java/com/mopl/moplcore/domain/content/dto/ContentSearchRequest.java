package com.mopl.moplcore.domain.content.dto;

import java.util.List;
import java.util.UUID;

import com.mopl.moplcore.domain.content.entity.Type;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentSearchRequest {

	private String keywordLike;
	private Type typeEqual;
	private List<String> tagsIn;
	private String cursor;
	private UUID idAfter;

	@NotNull(message = "limit은 필수입니다")
	@Min(value = 1, message = "limit은 1 이상이어야 합니다")
	private Integer limit;

	@NotNull(message = "sortDirection은 필수입니다")
	private SortDirection sortDirection;

	@NotNull(message = "sortBy는 필수입니다")
	private SortBy sortBy;

	public enum SortDirection {
		ASCENDING,
		DESCENDING
	}

	public enum SortBy {
		createdAt,
		watcherCount,
		rate
	}
}