package com.mopl.moplcore.domain.playlist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.playlist.entity.PlaylistContent;

public interface PlaylistContentRepository extends JpaRepository<PlaylistContent, UUID> {
	List<PlaylistContent> findByPlaylistId(UUID playlistId);
	
	Optional<PlaylistContent> findByPlaylistIdAndContentId(UUID playlistId, UUID contentId);
	
	void deleteByPlaylistIdAndContentId(UUID playlistId, UUID contentId);
}