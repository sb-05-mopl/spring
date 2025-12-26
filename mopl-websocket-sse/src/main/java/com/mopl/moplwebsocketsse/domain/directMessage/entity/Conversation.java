package com.mopl.moplwebsocketsse.domain.directMessage.entity;

import java.util.HashSet;
import java.util.Set;

import com.mopl.moplwebsocketsse.domain.common.entity.BaseEntity;
import com.mopl.moplwebsocketsse.domain.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "conversations")
public class Conversation extends BaseEntity {

	@OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
	private final Set<ConversationParticipants> members = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_message_id")
	private DirectMessage lastMessage;

	public Conversation(User user1, User user2) {
		this.members.add(new ConversationParticipants(this, user1));
		this.members.add(new ConversationParticipants(this, user2));
	}

	public void updateLastMessage(DirectMessage message) {
		this.lastMessage = message;
	}
}
