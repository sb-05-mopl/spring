package com.mopl.moplcore.domain.follow.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mopl.moplcore.domain.user.entity.Follow;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
	@Query(value = "SELECT EXISTS(SELECT 1 FROM follows WHERE follower_id = :followerId AND followee_id = :followeeId)",
		nativeQuery = true)
	boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

	@Query("SELECT COUNT(f) FROM Follow f WHERE f.followee.id = :followeeId")
	long countByFolloweeId(UUID followeeId);

	@Query(value = "SELECT follower_id FROM follows WHERE followee_id = :followeeID", nativeQuery = true)
	List<UUID> findFollowerIdsByFolloweeId(UUID followeeID);
}
