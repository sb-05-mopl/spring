package com.mopl.moplwebsocketsse.domain.directMessage.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.mopl.moplwebsocketsse.domain.directMessage.dto.DirectMessageSendRequest;
import com.mopl.moplwebsocketsse.domain.directMessage.service.DirectMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageController {

	private final DirectMessageService directMessageService;

	@MessageMapping("/conversations/{conversationId}/direct-messages")
	public void sendMessage(
		@DestinationVariable UUID conversationId,
		DirectMessageSendRequest request,
		Authentication authentication
	) {
		UUID senderId = (UUID)authentication.getPrincipal();

		log.debug("[DirectMessageController] Message received. conversationId={}, senderId={}",
			conversationId, senderId);

		directMessageService.sendMessage(conversationId, senderId, request.content());
	}
}