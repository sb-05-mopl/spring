package com.mopl.moplwebsocketsse.domain.directMessage.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.directMessage.dto.CursorResponseDirectMessageDto;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.DirectMessageDto;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.DirectMessageSearchRequest;
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

	@Transactional(readOnly = true)
	public CursorResponseDirectMessageDto findMessages(
		UUID conversationId,
		UUID requesterId,
		DirectMessageSearchRequest request
	) {
		boolean isParticipant = participantsRepository.existsByConversationIdAndUserId(conversationId, requesterId);
		if (!isParticipant) {
			throw new NotConversationParticipantException();
		}

		List<ConversationParticipants> participants =
			participantsRepository.findByConversationIdWithUser(conversationId);

		Map<UUID, User> userMap = participants.stream()
			.collect(Collectors.toMap(cp -> cp.getUser().getId(), ConversationParticipants::getUser));

		long totalCount = directMessageRepository.countByConversationId(conversationId);

		List<DirectMessage> messages = directMessageRepository.findByConversationIdWithCursor(
			conversationId,
			request.cursor(),
			request.idAfter(),
			request.limit(),
			request.sortDirection().name()
		);

		boolean hasNext = messages.size() > request.limit();
		List<DirectMessage> messagesLimit = hasNext ? messages.subList(0, request.limit()) : messages;

		List<DirectMessageDto> messageDtos = messagesLimit.stream()
			.map(dm -> {
				User sender = userMap.get(dm.getSender().getId());
				User receiver = userMap.values().stream()
					.filter(u -> !u.getId().equals(dm.getSender().getId()))
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("Receiver not found"));

				return conversationMapper.toDirectMessageDto(
					dm,
					new UserSummary(sender.getId(), sender.getName(), sender.getProfileImageUrl()),
					new UserSummary(receiver.getId(), receiver.getName(), receiver.getProfileImageUrl())
				);
			})
			.toList();

		String nextCursor = null;
		UUID nextIdAfter = null;

		if (hasNext) {
			DirectMessage lastMessage = messagesLimit.getLast();
			nextCursor = lastMessage.getCreatedAt().toString();
			nextIdAfter = lastMessage.getId();
		}

		return new CursorResponseDirectMessageDto(
			messageDtos,
			nextCursor,
			nextIdAfter != null ? nextIdAfter.toString() : null,
			hasNext,
			totalCount,
			"createdAt",
			request.sortDirection()
		);
	}

	@Transactional
	public void markAsRead(UUID conversationId, UUID directMessageId, UUID requesterId) {
		boolean isParticipant = participantsRepository.existsByConversationIdAndUserId(conversationId, requesterId);
		if (!isParticipant) {
			throw new NotConversationParticipantException();
		}

		directMessageRepository.markAllAsReadInConversation(conversationId, requesterId);

		log.debug("[DirectMessageService] message read. conversationId={}, readerId={}",
			conversationId, requesterId);
	}
}