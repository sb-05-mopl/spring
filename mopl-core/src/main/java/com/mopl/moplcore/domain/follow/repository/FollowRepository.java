package com.mopl.moplcore.domain.follow.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.user.entity.Follow;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

	boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

	long countByFolloweeId(UUID followeeId);
}
