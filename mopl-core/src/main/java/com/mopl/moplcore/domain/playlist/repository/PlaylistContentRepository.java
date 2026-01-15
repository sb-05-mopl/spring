package com.mopl.moplcore.domain.playlist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mopl.moplcore.domain.playlist.entity.PlaylistContent;

public interface PlaylistContentRepository extends JpaRepository<PlaylistContent, UUID> {
	@Query("""
    SELECT pc FROM PlaylistContent pc
    JOIN FETCH pc.content
    WHERE pc.playlist.id = :playlistId
    """)
	List<PlaylistContent> findByPlaylistId(UUID playlistId);

	@Query("""
    SELECT pc FROM PlaylistContent pc
    JOIN FETCH pc.content
    WHERE pc.playlist.id IN :playlistIds
    """)
	List<PlaylistContent> findByPlaylistIds(List<UUID> playlistIds);
	
	Optional<PlaylistContent> findByPlaylistIdAndContentId(UUID playlistId, UUID contentId);
	
	void deleteByPlaylistIdAndContentId(UUID playlistId, UUID contentId);
}