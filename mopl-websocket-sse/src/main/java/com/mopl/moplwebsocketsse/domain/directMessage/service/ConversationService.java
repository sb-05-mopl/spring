package com.mopl.moplwebsocketsse.domain.directMessage.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationCreatedLockResult;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationDto;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationSearchRequest;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.CursorResponseConversationDto;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.DirectMessageDto;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.ConversationParticipants;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.DirectMessage;
import com.mopl.moplwebsocketsse.domain.directMessage.event.ConversationCreatedLockEvent;
import com.mopl.moplwebsocketsse.domain.directMessage.exception.ConversationLockAcquisitionFailedException;
import com.mopl.moplwebsocketsse.domain.directMessage.exception.ConversationNotFoundException;
import com.mopl.moplwebsocketsse.domain.directMessage.exception.NotConversationParticipantException;
import com.mopl.moplwebsocketsse.domain.directMessage.exception.SelfConversationNotAllowedException;
import com.mopl.moplwebsocketsse.domain.directMessage.mapper.ConversationMapper;
import com.mopl.moplwebsocketsse.domain.directMessage.repository.ConversationParticipantsRepository;
import com.mopl.moplwebsocketsse.domain.directMessage.repository.ConversationRepository;
import com.mopl.moplwebsocketsse.domain.directMessage.repository.DirectMessageRepository;
import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;
import com.mopl.moplwebsocketsse.domain.user.entity.User;
import com.mopl.moplwebsocketsse.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

	private final ConversationRepository conversationRepository;
	private final ConversationParticipantsRepository participantsRepository;
	private final DirectMessageRepository directMessageRepository;
	private final UserRepository userRepository;
	private final ConversationMapper conversationMapper;
	private final ConversationLockService conversationLockService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public ConversationDto createConversation(UUID requesterId, UUID withUserId) {
		if (requesterId.equals(withUserId)) {
			throw new SelfConversationNotAllowedException();
		}

		Optional<ConversationCreatedLockResult> lockResultOpt = conversationLockService.tryLock(requesterId,
			withUserId);

		if (lockResultOpt.isEmpty()) {
			return participantsRepository.findConversationBetween(requesterId, withUserId)
				.map(conv -> findConversationInternal(conv.getId(), requesterId))
				.orElseThrow(ConversationLockAcquisitionFailedException::new);
		}

		ConversationCreatedLockResult lockResult = lockResultOpt.get();

		eventPublisher.publishEvent(new ConversationCreatedLockEvent(lockResult.key(), lockResult.value()));

		Optional<Conversation> existing = participantsRepository.findConversationBetween(requesterId, withUserId);
		if (existing.isPresent()) {
			return findConversationInternal(existing.get().getId(), requesterId);
		}

		User requester = userRepository.findById(requesterId)
			.orElseThrow(() -> new IllegalArgumentException("user not found, requestId: " + requesterId));
		User withUser = userRepository.findById(withUserId)
			.orElseThrow(() -> new IllegalArgumentException("user not found, withUserId: " + withUserId));

		Conversation conversation = new Conversation();
		conversationRepository.save(conversation);

		participantsRepository.save(new ConversationParticipants(conversation, requester));
		participantsRepository.save(new ConversationParticipants(conversation, withUser));

		log.debug("[ConversationService] Created conversation. conversationId={}, requesterId={}, withUserId={}",
			conversation.getId(), requesterId, withUserId);

		return conversationMapper.toConversationDto(
			conversation,
			new UserSummary(withUser.getId(), withUser.getName(), withUser.getProfileImageUrl()),
			null,
			false
		);
	}

	public ConversationDto findConversation(UUID conversationId, UUID requesterId) {
		boolean exists = conversationRepository.existsById(conversationId);
		if (!exists) {
			throw new ConversationNotFoundException();
		}

		boolean isParticipant = participantsRepository.existsByConversationIdAndUserId(conversationId, requesterId);
		if (!isParticipant) {
			throw new NotConversationParticipantException();
		}

		return findConversationInternal(conversationId, requesterId);
	}

	public ConversationDto findConversationWith(UUID requesterId, UUID withUserId) {
		Conversation conversation = participantsRepository.findConversationBetween(requesterId, withUserId)
			.orElseThrow(ConversationNotFoundException::new);

		return findConversationInternal(conversation.getId(), requesterId);
	}

	public CursorResponseConversationDto findConversations(UUID requesterId, ConversationSearchRequest request) {
		long totalCount = conversationRepository.countByParticipantId(requesterId, request.keywordLike());

		List<Conversation> conversations = conversationRepository.findByParticipantIdWithCursor(
			requesterId,
			request.keywordLike(),
			request.cursor(),
			request.idAfter(),
			request.limit(),
			request.sortDirection().name()
		);

		boolean hasNext = conversations.size() > request.limit();
		List<Conversation> conversationsLimit = hasNext ? conversations.subList(0, request.limit()) : conversations;

		if (conversationsLimit.isEmpty()) {
			return new CursorResponseConversationDto(
				List.of(),
				null,
				null,
				false,
				totalCount,
				"createdAt",
				request.sortDirection()
			);
		}

		List<UUID> conversationIds = conversationsLimit.stream()
			.map(Conversation::getId)
			.toList();

		Map<UUID, List<ConversationParticipants>> participantsMap =
			participantsRepository.findByConversationIdInWithUser(conversationIds)
				.stream()
				.collect(Collectors.groupingBy(cp -> cp.getConversation().getId()));

		Set<UUID> unreadConversationIds = new HashSet<>(
			directMessageRepository.findConversationIdsWithUnreadMessages(conversationIds, requesterId)
		);

		Map<UUID, Conversation> conversationWithLastMessageMap =
			conversationRepository.findByIdInWithLastMessage(conversationIds)
				.stream()
				.collect(Collectors.toMap(Conversation::getId, c -> c));

		List<ConversationDto> conversationDtos = conversationsLimit.stream()
			.map(conv -> buildConversationDto(
				conversationWithLastMessageMap.get(conv.getId()),
				requesterId,
				participantsMap,
				unreadConversationIds
			))
			.toList();

		String nextCursor = null;
		UUID nextIdAfter = null;

		if (hasNext) {
			Conversation lastConversation = conversationsLimit.getLast();
			nextCursor = lastConversation.getCreatedAt().toString();
			nextIdAfter = lastConversation.getId();
		}

		return new CursorResponseConversationDto(
			conversationDtos,
			nextCursor,
			nextIdAfter != null ? nextIdAfter.toString() : null,
			hasNext,
			totalCount,
			"createdAt",
			request.sortDirection()
		);
	}

	private ConversationDto findConversationInternal(UUID conversationId, UUID requesterId) {
		List<UUID> conversationIds = List.of(conversationId);

		Map<UUID, List<ConversationParticipants>> participantsMap =
			participantsRepository.findByConversationIdInWithUser(conversationIds)
				.stream()
				.collect(Collectors.groupingBy(cp -> cp.getConversation().getId()));

		Set<UUID> unreadConversationIds = new HashSet<>(
			directMessageRepository.findConversationIdsWithUnreadMessages(conversationIds, requesterId)
		);

		Conversation conversation = conversationRepository.findByIdWithLastMessage(conversationId)
			.orElseThrow(ConversationNotFoundException::new);

		return buildConversationDto(conversation, requesterId, participantsMap, unreadConversationIds);
	}

	private ConversationDto buildConversationDto(
		Conversation conversation,
		UUID requesterId,
		Map<UUID, List<ConversationParticipants>> participantsMap,
		Set<UUID> unreadConversationIds
	) {
		List<ConversationParticipants> participants = participantsMap.get(conversation.getId());

		if (participants == null || participants.size() != 2) {
			throw new IllegalStateException("Invalid conversation participants count: " +
				(participants == null ? 0 : participants.size()));
		}

		User user1 = participants.get(0).getUser();
		User user2 = participants.get(1).getUser();

		if (!requesterId.equals(user1.getId()) && !requesterId.equals(user2.getId())) {
			throw new IllegalStateException("Requester is not a participant: " + requesterId);
		}

		User withUser = user1.getId().equals(requesterId) ? user2 : user1;

		DirectMessageDto lastMessageDto = null;
		DirectMessage lastMessage = conversation.getLastMessage();
		if (lastMessage != null) {
			UUID senderId = lastMessage.getSender().getId();

			if (!senderId.equals(user1.getId()) && !senderId.equals(user2.getId())) {
				throw new IllegalStateException("Message sender is not a participant: " + senderId);
			}

			User sender = senderId.equals(user1.getId()) ? user1 : user2;
			User receiver = senderId.equals(user1.getId()) ? user2 : user1;

			lastMessageDto = conversationMapper.toDirectMessageDto(
				lastMessage,
				new UserSummary(sender.getId(), sender.getName(), sender.getProfileImageUrl()),
				new UserSummary(receiver.getId(), receiver.getName(), receiver.getProfileImageUrl())
			);
		}

		boolean hasUnread = unreadConversationIds.contains(conversation.getId());

		return conversationMapper.toConversationDto(
			conversation,
			new UserSummary(withUser.getId(), withUser.getName(), withUser.getProfileImageUrl()),
			lastMessageDto,
			hasUnread
		);
	}
}