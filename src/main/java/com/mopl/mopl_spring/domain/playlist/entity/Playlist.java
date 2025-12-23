package com.mopl.mopl_spring.domain.playlist.entity;

import com.mopl.mopl_spring.domain.common.entity.BaseUpdatableEntity;
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
@Table(name = "playlists")
public class Playlist extends BaseUpdatableEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	public Playlist(User owner, String title, String description) {
		this.owner = owner;
		this.title = title;
		this.description = description;
	}

	public void update(String title, String description) {
		this.title = title;
		this.description = description;
	}
}
