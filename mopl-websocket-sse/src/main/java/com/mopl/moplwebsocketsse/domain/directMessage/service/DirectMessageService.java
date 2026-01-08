package com.mopl.moplwebsocketsse.domain.directMessage.service;

import java.util.List;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.directMessage.dto.DirectMessageDto;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.ConversationParticipants;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.DirectMessage;
import com.mopl.moplwebsocketsse.domain.directMessage.exception.ConversationNotFoundException;
import com.mopl.moplwebsocketsse.domain.directMessage.exception.NotConversationParticipantException;
import com.mopl.moplwebsocketsse.domain.directMessage.mapper.ConversationMapper;
import com.mopl.moplwebsocketsse.domain.directMessage.repository.ConversationParticipantsRepository;
import com.mopl.moplwebsocketsse.domain.directMessage.repository.ConversationRepository;
import com.mopl.moplwebsocketsse.domain.directMessage.repository.DirectMessageRepository;
import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;
import com.mopl.moplwebsocketsse.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageService {

	private static final String DM_DEST_PREFIX = "/sub/conversations/";
	private static final String DM_DEST_SUFFIX = "/direct-messages";

	private final DirectMessageRepository directMessageRepository;
	private final ConversationRepository conversationRepository;
	private final ConversationParticipantsRepository participantsRepository;
	private final ConversationMapper conversationMapper;
	private final SimpMessagingTemplate messagingTemplate;

	@Transactional
	public DirectMessageDto sendMessage(UUID conversationId, UUID senderId, String content) {
		Conversation conversation = conversationRepository.findById(conversationId)
			.orElseThrow(ConversationNotFoundException::new);

		List<ConversationParticipants> participants =
			participantsRepository.findByConversationIdWithUser(conversationId);

		User sender = null;
		User receiver = null;

		for (ConversationParticipants cp : participants) {
			if (cp.getUser().getId().equals(senderId)) {
				sender = cp.getUser();
			} else {
				receiver = cp.getUser();
			}
		}

		if (sender == null) {
			throw new NotConversationParticipantException();
		}

		if (receiver == null) {
			throw new IllegalStateException("Receiver not found in conversation: " + conversationId);
		}

		DirectMessage directMessage = new DirectMessage(conversation, sender, content);
		directMessageRepository.save(directMessage);

		conversation.updateLastMessage(directMessage);

		DirectMessageDto dto = conversationMapper.toDirectMessageDto(
			directMessage,
			new UserSummary(sender.getId(), sender.getName(), sender.getProfileImageUrl()),
			new UserSummary(receiver.getId(), receiver.getName(), receiver.getProfileImageUrl())
		);

		// 브로드캐스트
		String destination = DM_DEST_PREFIX + conversationId + DM_DEST_SUFFIX;
		messagingTemplate.convertAndSend(destination, dto);

		log.debug("[DirectMessageService] Message sent. conversationId={}, senderId={}, receiverId={}",
			conversationId, senderId, receiver.getId());

		return dto;
	}
}