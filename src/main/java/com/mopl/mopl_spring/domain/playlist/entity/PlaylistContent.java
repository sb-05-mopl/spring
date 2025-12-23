package com.mopl.mopl_spring.domain.playlist.entity;

import com.mopl.mopl_spring.domain.common.entity.BaseEntity;
import com.mopl.mopl_spring.domain.content.entity.Content;

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
@Table(name = "playlist_contents")
public class PlaylistContent extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "playlist_id", nullable = false)
	private Playlist playlist;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "content_id", nullable = false)
	private Content content;

	public PlaylistContent(Playlist playlist, Content content) {
		this.playlist = playlist;
		this.content = content;
	}
}
