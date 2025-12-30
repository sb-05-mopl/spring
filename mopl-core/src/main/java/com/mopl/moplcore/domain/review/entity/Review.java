package com.mopl.moplcore.domain.review.entity;


import com.mopl.moplcore.domain.common.entity.BaseUpdatableEntity;
import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.review.exception.InvalidRatingException;
import com.mopl.moplcore.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reviews")
public class Review extends BaseUpdatableEntity {
	private static final double MIN_RATING = 1.0;
	private static final double MAX_RATING = 5.0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "content_id", nullable = false)
	private Content content;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String text;

	@Column(nullable = false)
	private double rating;

	@Builder
	public Review(User author, Content content, String text, double rating) {
		validateRating(rating);
		this.author = author;
		this.content = content;
		this.text = text;
		this.rating = rating;
	}

	public void update(String text, Double rating) {
		if(text != null) {
			this.text = text;
		}
		if(rating != null) {
			validateRating(rating);
			this.rating = rating;
		}
	}

	private void validateRating(double rating) {
		if (rating < MIN_RATING || rating > MAX_RATING) {
			throw InvalidRatingException.withRating(rating);
		}
	}
}
