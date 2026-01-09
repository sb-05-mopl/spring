package com.mopl.moplwebsocketsse.domain.directMessage.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationCreateRequest;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationDto;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationSearchRequest;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.CursorResponseConversationDto;
import com.mopl.moplwebsocketsse.domain.directMessage.service.ConversationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

	private final ConversationService conversationService;

	@PostMapping
	public ResponseEntity<ConversationDto> create(
		@RequestBody @Valid ConversationCreateRequest request,
		@AuthenticationPrincipal UUID requesterId
	) {
		ConversationDto response = conversationService.createConversation(requesterId, request.withUserId());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<CursorResponseConversationDto> findConversations(
		@Valid ConversationSearchRequest request,
		@AuthenticationPrincipal UUID requesterId
	) {
		CursorResponseConversationDto response = conversationService.findConversations(requesterId, request);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{conversationId}")
	public ResponseEntity<ConversationDto> findConversation(
		@PathVariable UUID conversationId,
		@AuthenticationPrincipal UUID requesterId
	) {
		ConversationDto response = conversationService.findConversation(conversationId, requesterId);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/with")
	public ResponseEntity<ConversationDto> findConversationWith(
		@RequestParam UUID userId,
		@AuthenticationPrincipal UUID requesterId
	) {
		ConversationDto response = conversationService.findConversationWith(requesterId, userId);

		return ResponseEntity.ok(response);
	}
}