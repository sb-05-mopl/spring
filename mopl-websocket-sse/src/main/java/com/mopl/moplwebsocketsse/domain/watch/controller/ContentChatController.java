package com.mopl.moplwebsocketsse.domain.watch.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.mopl.moplwebsocketsse.domain.watch.dto.ContentChatSendRequest;
import com.mopl.moplwebsocketsse.domain.watch.service.ContentChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ContentChatController {

	private final ContentChatService contentChatService;

	@MessageMapping("/contents/{contentId}/chat")
	public void sendChatMessage(
		@DestinationVariable UUID contentId,
		ContentChatSendRequest request,
		Authentication authentication
	) {
		UUID userId = (UUID)authentication.getPrincipal();

		log.debug("[ContentChatController] Chat message received. contentId={}, userId={}, contentLength={}",
			contentId, userId, request.content().length());

		contentChatService.broadcastChatMessage(contentId, userId, request.content());
	}
}
