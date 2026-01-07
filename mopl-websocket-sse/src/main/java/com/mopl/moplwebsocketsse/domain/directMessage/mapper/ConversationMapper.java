package com.mopl.moplwebsocketsse.domain.directMessage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationDto;
import com.mopl.moplwebsocketsse.domain.directMessage.dto.DirectMessageDto;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;
import com.mopl.moplwebsocketsse.domain.directMessage.entity.DirectMessage;
import com.mopl.moplwebsocketsse.domain.user.dto.UserSummary;

@Mapper(
	componentModel = "spring",
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ConversationMapper {

	default ConversationDto toConversationDto(Conversation conversation, UserSummary withUser,
		DirectMessageDto lastMessage, boolean hasUnread) {
		return new ConversationDto(
			conversation.getId(),
			withUser,
			lastMessage,
			hasUnread
		);
	}

	@Mapping(source = "dm.id", target = "id")
	@Mapping(source = "dm.conversation.id", target = "conversationId")
	@Mapping(source = "dm.createdAt", target = "createdAt")
	@Mapping(source = "sender", target = "sender")
	@Mapping(source = "receiver", target = "receiver")
	@Mapping(source = "dm.content", target = "content")
	DirectMessageDto toDirectMessageDto(DirectMessage dm, UserSummary sender, UserSummary receiver);
}