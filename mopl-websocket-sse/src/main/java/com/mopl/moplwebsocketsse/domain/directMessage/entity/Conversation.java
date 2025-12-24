package com.mopl.moplwebsocketsse.domain.directMessage.entity;


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
@Table(name = "conversations")
public class Conversation extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user1_id", nullable = false)
	private User user1;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user2_id", nullable = false)
	private User user2;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_message_id")
	private DirectMessage lastMessage;

	public Conversation(User user1, User user2) {
		this.user1 = user1;
		this.user2 = user2;
	}

	public void updateLastMessage(DirectMessage message) {
		this.lastMessage = message;
	}
}
