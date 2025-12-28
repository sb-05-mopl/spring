package com.mopl.moplwebsocketsse.domain.directMessage.entity;

import java.time.Instant;

import com.mopl.moplwebsocketsse.domain.common.entity.BaseEntity;
import com.mopl.moplwebsocketsse.domain.user.entity.User;

import jakarta.persistence.Column;
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
@Table(name = "direct_messages")
public class DirectMessage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conversation_id", nullable = false)
	private Conversation conversation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "read_at")
	private Instant readAt;

	public DirectMessage(Conversation conversation, User sender, String content) {
		this.conversation = conversation;
		this.sender = sender;
		this.content = content;
		this.readAt = null;
	}

	public boolean isRead() {
		return readAt != null;
	}

	public void markAsRead() {
		this.readAt = Instant.now();
	}
}
