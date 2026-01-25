package com.mopl.moplcore.domain.playlist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mopl.moplcore.domain.playlist.entity.PlaylistSubscription;

public interface PlaylistSubscriptionRepository extends JpaRepository<PlaylistSubscription, UUID> {
	Long countByPlaylistId(UUID playlistId);

	Optional<PlaylistSubscription> findByPlaylistIdAndUserId(UUID playlistId, UUID userId);

	@Modifying
	@Query("DELETE FROM PlaylistSubscription ps WHERE ps.playlist.id = :playlistId AND ps.user.id = :userId")
	void deleteByPlaylistIdAndUserId(@Param("playlistId") UUID playlistId, @Param("userId") UUID userId);

	boolean existsByPlaylistIdAndUserId(UUID playlistId, UUID userId);

	@Query("SELECT ps.user.id FROM PlaylistSubscription ps WHERE ps.playlist.id = :playlistId")
	List<UUID> findSubscriberIdsByPlaylistId(@Param("playlistId") UUID playlistId);
}