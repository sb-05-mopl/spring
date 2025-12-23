package com.mopl.mopl_spring.domain.content.entity;

import com.mopl.mopl_spring.domain.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "contents")
public class Content extends BaseEntity {
	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Type type;

	@Column(name = "thumbnail_url")
	private String thumbnailUrl;

	@Column(name = "average_rating", nullable = false)
	private double averageRating;

	@Column(name = "review_count", nullable = false)
	private int reviewCount;

	public Content(String title, String description, Type type, String thumbnailUrl) {
		this.title = title;
		this.description = description;
		this.type = type;
		this.thumbnailUrl = thumbnailUrl;
		this.averageRating = 0.0;
		this.reviewCount = 0;
	}

	public void update(String title, String description, Type type, String thumbnailUrl) {
		this.title = title;
		this.description = description;
		this.type = type;
		this.thumbnailUrl = thumbnailUrl;
	}

	public void updateRating(double averageRating, int reviewCount) {
		this.averageRating = averageRating;
		this.reviewCount = reviewCount;
	}
}
