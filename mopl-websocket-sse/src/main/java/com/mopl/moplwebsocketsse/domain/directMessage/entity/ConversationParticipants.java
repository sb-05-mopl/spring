package com.mopl.moplwebsocketsse.domain.directMessage.entity;

import java.time.Instant;

import com.mopl.moplwebsocketsse.domain.common.entity.BaseEntity;
import com.mopl.moplwebsocketsse.domain.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "conversation_participants")
public class ConversationParticipants extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conversation_id", nullable = false)
	private Conversation conversation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private Instant last_read_at;

	protected ConversationParticipants(Conversation conversation, User user) {
		this.conversation = conversation;
		this.user = user;
	}

	public void updateLastReadAt(Instant LastReadAt) {
		this.last_read_at = LastReadAt;
	}
}
