package com.mopl.moplcore.domain.follow.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplcore.domain.follow.dto.FollowDto;
import com.mopl.moplcore.domain.follow.dto.FollowRequest;
import com.mopl.moplcore.domain.follow.service.FollowService;
import com.mopl.moplcore.security.principal.MoplUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

	private final FollowService followService;

	@PostMapping
	public ResponseEntity<FollowDto> createFollow(
		@RequestBody @Valid FollowRequest request,
		@AuthenticationPrincipal MoplUserDetails userDetails
	) {
		UUID followerId = userDetails.getUserDto().getId();
		FollowDto response = followService.create(request, followerId);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/{followId}")
	public ResponseEntity<Void> cancelFollow(
		@PathVariable UUID followId,
		@AuthenticationPrincipal MoplUserDetails userDetails
	) {
		UUID followerId = userDetails.getUserDto().getId();
		followService.cancel(followId, followerId);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/followed-by-me")
	public ResponseEntity<Boolean> isFollowedByMe(
		@RequestParam UUID followeeId,
		@AuthenticationPrincipal MoplUserDetails userDetails
	) {
		UUID followerId = userDetails.getUserDto().getId();
		boolean isFollowed = followService.isFollowedByMe(followerId, followeeId);

		return ResponseEntity.ok(isFollowed);
	}

	@GetMapping("/count")
	public ResponseEntity<Long> countFollowers(@RequestParam UUID followeeId) {
		long followerCount = followService.countFollowers(followeeId);

		return ResponseEntity.ok(followerCount);
	}
}
