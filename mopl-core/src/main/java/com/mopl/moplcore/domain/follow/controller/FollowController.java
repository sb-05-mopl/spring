package com.mopl.moplcore.domain.follow.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplcore.domain.follow.dto.FollowDto;
import com.mopl.moplcore.domain.follow.dto.FollowRequest;
import com.mopl.moplcore.domain.follow.service.FollowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

	private final FollowService followService;

	private final UUID TEMP_FOLLOWER_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

	@PostMapping
	public ResponseEntity<FollowDto> createFollow(@RequestBody @Valid FollowRequest request) {
		FollowDto response = followService.create(request, TEMP_FOLLOWER_ID);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/{followId}")
	public ResponseEntity<Void> cancelFollow(@PathVariable UUID followId) {
		followService.cancel(followId, TEMP_FOLLOWER_ID);

		return ResponseEntity.noContent().build();
	}
}
