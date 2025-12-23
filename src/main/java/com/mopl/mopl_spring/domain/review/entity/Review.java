package com.mopl.mopl_spring.domain.review.entity;

import com.mopl.mopl_spring.domain.common.entity.BaseUpdatableEntity;
import com.mopl.mopl_spring.domain.content.entity.Content;
import com.mopl.mopl_spring.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reviews")
public class Review extends BaseUpdatableEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "content_id", nullable = false)
	private Content contentId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String text;

	@Column(nullable = false)
	private double rating;

	public Review(User user, Content content, String text, double rating) {
		this.user = user;
		this.contentId = content;
		this.text = text;
		this.rating = rating;
	}

	public void update(String text, double rating) {
		this.text = text;
		this.rating = rating;
	}
}
