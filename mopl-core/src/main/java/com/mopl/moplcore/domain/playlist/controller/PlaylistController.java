package com.mopl.moplcore.domain.playlist.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplcore.domain.playlist.dto.CursorResponsePlaylistDto;
import com.mopl.moplcore.domain.playlist.dto.PlaylistCreateRequest;
import com.mopl.moplcore.domain.playlist.dto.PlaylistDto;
import com.mopl.moplcore.domain.playlist.dto.PlaylistSearchRequest;
import com.mopl.moplcore.domain.playlist.dto.PlaylistUpdateRequest;
import com.mopl.moplcore.domain.playlist.service.PlaylistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

	private final PlaylistService playlistService;

	private UUID getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof UUID) {
			return (UUID) principal;
		}

		return null;
	}

	@Operation(summary = "플레이리스트 목록 조회")
	@GetMapping
	public ResponseEntity<CursorResponsePlaylistDto> searchPlaylists(
		@Valid @ModelAttribute PlaylistSearchRequest request
	) {
		UUID currentUserId = getCurrentUserId();
		CursorResponsePlaylistDto response = playlistService.searchPlaylists(request, currentUserId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "플레이리스트 상세 조회")
	@GetMapping("/{playlistId}")
	public ResponseEntity<PlaylistDto> getPlaylist(
		@PathVariable UUID playlistId
	) {
		UUID currentUserId = getCurrentUserId();
		PlaylistDto response = playlistService.getPlaylist(playlistId, currentUserId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "플레이리스트 생성")
	@PostMapping
	public ResponseEntity<PlaylistDto> createPlaylist(
		@Valid @RequestBody PlaylistCreateRequest request
	) {
		UUID currentUserId = getCurrentUserId();
		PlaylistDto response = playlistService.createPlaylist(currentUserId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "플레이리스트 수정")
	@PatchMapping("/{playlistId}")
	public ResponseEntity<PlaylistDto> updatePlaylist(
		@PathVariable UUID playlistId,
		@Valid @RequestBody PlaylistUpdateRequest request
	) {
		UUID currentUserId = getCurrentUserId();
		PlaylistDto response = playlistService.updatePlaylist(playlistId, currentUserId, request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "플레이리스트 삭제")
	@DeleteMapping("/{playlistId}")
	public ResponseEntity<Void> deletePlaylist(
		@PathVariable UUID playlistId
	) {
		UUID currentUserId = getCurrentUserId();
		playlistService.deletePlaylist(playlistId, currentUserId);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "플레이리스트에 콘텐츠 추가")
	@PostMapping("/{playlistId}/contents/{contentId}")
	public ResponseEntity<Void> addContentToPlaylist(
		@PathVariable UUID playlistId,
		@PathVariable UUID contentId
	) {
		UUID currentUserId = getCurrentUserId();
		playlistService.addContentToPlaylist(playlistId, contentId, currentUserId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Operation(summary = "플레이리스트에서 콘텐츠 제거")
	@DeleteMapping("/{playlistId}/contents/{contentId}")
	public ResponseEntity<Void> removeContentFromPlaylist(
		@PathVariable UUID playlistId,
		@PathVariable UUID contentId
	) {
		UUID currentUserId = getCurrentUserId();
		playlistService.removeContentFromPlaylist(playlistId, contentId, currentUserId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Operation(summary = "플레이리스트 구독")
	@PostMapping("/{playlistId}/subscription")
	public ResponseEntity<Void> subscribePlaylist(
		@PathVariable UUID playlistId
	) {
		UUID currentUserId = getCurrentUserId();
		playlistService.subscribePlaylist(playlistId, currentUserId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Operation(summary = "플레이리스트 구독 취소")
	@DeleteMapping("/{playlistId}/subscription")
	public ResponseEntity<Void> unsubscribePlaylist(
		@PathVariable UUID playlistId
	) {
		UUID currentUserId = getCurrentUserId();
		playlistService.unsubscribePlaylist(playlistId, currentUserId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}