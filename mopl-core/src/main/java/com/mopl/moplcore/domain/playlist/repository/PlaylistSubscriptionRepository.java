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
	// subscriberCount 배치
	@Query("""
    SELECT ps.playlist.id, COUNT(ps) 
    FROM PlaylistSubscription ps 
    WHERE ps.playlist.id IN :playlistIds 
    GROUP BY ps.playlist.id
    """)
	List<Object[]> countByPlaylistIds(List<UUID> playlistIds);

	// subscribedByMe 배치
	@Query("""
    SELECT ps.playlist.id 
    FROM PlaylistSubscription ps 
    WHERE ps.playlist.id IN :playlistIds 
    AND ps.user.id = :userId
    """)
	List<UUID> findSubscribedPlaylistIds(
		List<UUID> playlistIds,
		UUID userId
	);
	Long countByPlaylistId(UUID playlistId);

	Optional<PlaylistSubscription> findByPlaylistIdAndUserId(UUID playlistId, UUID userId);

	@Modifying
	@Query("DELETE FROM PlaylistSubscription ps WHERE ps.playlist.id = :playlistId AND ps.user.id = :userId")
	void deleteByPlaylistIdAndUserId(@Param("playlistId") UUID playlistId, @Param("userId") UUID userId);

	boolean existsByPlaylistIdAndUserId(UUID playlistId, UUID userId);

	@Query("SELECT ps.user.id FROM PlaylistSubscription ps WHERE ps.playlist.id = :playlistId")
	List<UUID> findSubscriberIdsByPlaylistId(@Param("playlistId") UUID playlistId);

	void deleteByPlaylistId(UUID playlistId);
}