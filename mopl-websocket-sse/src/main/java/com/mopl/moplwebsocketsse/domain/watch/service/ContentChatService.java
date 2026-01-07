package com.mopl.moplwebsocketsse.domain.watch.service;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;
import com.mopl.moplwebsocketsse.domain.user.entity.User;
import com.mopl.moplwebsocketsse.domain.user.repository.UserRepository;
import com.mopl.moplwebsocketsse.domain.watch.dto.ContentChatDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentChatService {

	private static final String CHAT_DEST_PREFIX = "/sub/contents/";
	private static final String CHAT_DEST_SUFFIX = "/chat";

	private final SimpMessagingTemplate messagingTemplate;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public void broadcastChatMessage(UUID contentId, UUID senderId, String content) {
		if (contentId == null) {
			throw new IllegalArgumentException("contentId is null");
		}
		if (senderId == null) {
			throw new IllegalArgumentException("senderId is null");
		}
		if (content == null) {
			throw new IllegalArgumentException("content is null");
		}

		User user = userRepository.findById(senderId)
			.orElseThrow(() -> new IllegalArgumentException("User not found: " + senderId));

		UserSummary sender = new UserSummary(user.getId(), user.getName(), user.getProfileImageUrl());

		ContentChatDto chatDto = new ContentChatDto(sender, content);

		// 3) 브로드캐스트
		String destination = chatDestination(contentId);
		messagingTemplate.convertAndSend(destination, chatDto);

		log.debug("[ContentChatService] Chat message broadcasted. destination={}, senderId={}, contentLength={}",
			destination, senderId, content.length());
	}

	private String chatDestination(UUID contentId) {
		return CHAT_DEST_PREFIX + contentId + CHAT_DEST_SUFFIX;
	}
}
