package com.mopl.moplcore.domain.content.entity;

import com.mopl.moplcore.domain.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "contents",
	uniqueConstraints = @UniqueConstraint(name = "uk_type_source_id", columnNames = {"source_id", "type"})
)
public class Content extends BaseEntity {
	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "thumbnail_url")
	private String thumbnailUrl;

	@Column(name = "average_rating", nullable = false)
	private double averageRating;

	@Column(name = "review_count", nullable = false)
	private int reviewCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Type type;

	@Column(nullable = false)
	private long sourceId;

	@Builder
	public Content(String title, String description, Type type, String thumbnailUrl, long sourceId) {
		this.title = title;
		this.description = description;
		this.type = type;
		this.thumbnailUrl = thumbnailUrl;
		this.averageRating = 0.0;
		this.reviewCount = 0;
		this.sourceId = sourceId;
	}

	public void update(String title, String description, Type type, String thumbnailUrl) {
		this.title = title;
		this.description = description;
		this.type = type;
		this.thumbnailUrl = thumbnailUrl;
	}

	public void updateTitle(String title) {
		this.title = title;
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void updateThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public void updateRating(double averageRating, int reviewCount) {
		this.averageRating = averageRating;
		this.reviewCount = reviewCount;
	}
}