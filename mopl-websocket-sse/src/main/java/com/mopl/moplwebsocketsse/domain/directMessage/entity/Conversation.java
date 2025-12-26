package com.mopl.moplwebsocketsse.domain.directMessage.entity;

import com.mopl.moplwebsocketsse.domain.common.entity.BaseEntity;

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
	@JoinColumn(name = "last_message_id")
	private DirectMessage lastMessage;

	public void updateLastMessage(DirectMessage message) {
		this.lastMessage = message;
	}
}
