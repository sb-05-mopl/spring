package com.mopl.moplcore.domain.follow.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.follow.dto.FollowDto;
import com.mopl.moplcore.domain.follow.dto.FollowRequest;
import com.mopl.moplcore.domain.follow.exception.CannotFollowYourselfException;
import com.mopl.moplcore.domain.follow.exception.FollowAlreadyExistsException;
import com.mopl.moplcore.domain.follow.exception.FollowNotFoundException;
import com.mopl.moplcore.domain.follow.exception.ForbiddenFollowAccessException;
import com.mopl.moplcore.domain.follow.mapper.FollowMapper;
import com.mopl.moplcore.domain.follow.repository.FollowRepository;
import com.mopl.moplcore.domain.user.entity.Follow;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final FollowMapper followMapper;

	@Transactional
	public FollowDto create(FollowRequest followRequest, UUID followerId) {

		UUID followeeId = followRequest.followeeId();
		if (followerId.equals(followeeId)) {
			throw new CannotFollowYourselfException();
		}

		if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
			throw new FollowAlreadyExistsException();
		}

		User follower = userRepository.findById(followerId)
			.orElseThrow(() -> new RuntimeException("Follower not found"));
		User followee = userRepository.findById(followeeId)
			.orElseThrow(() -> new RuntimeException("Followee not found"));

		Follow follow = new Follow(follower, followee);
		Follow savedFollow = followRepository.save(follow);

		return followMapper.toDto(savedFollow);
	}

	@Transactional
	public void cancel(UUID followId, UUID requesterId) {

		Follow follow = followRepository.findById(followId)
			.orElseThrow(() -> FollowNotFoundException.withFollowId(followId));

		if (!follow.getFollower().getId().equals(requesterId)) {
			throw new ForbiddenFollowAccessException();
		}

		followRepository.delete(follow);
	}

	@Transactional(readOnly = true)
	public boolean isFollowedByMe(UUID followerId, UUID followeeId) {
		return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
	}

	@Transactional(readOnly = true)
	public long countFollowers(UUID followeeId) {
		return followRepository.countByFolloweeId(followeeId);
	}
}
