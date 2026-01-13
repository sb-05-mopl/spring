package com.mopl.moplwebsocketsse.domain.notification.entity;

import java.util.UUID;

import com.mopl.moplwebsocketsse.domain.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

	@Column(name = "receiver_id", nullable = false)
	private UUID receiverId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationLevel level;

	@Setter(AccessLevel.PUBLIC)
	@Column(name = "event_id", unique = true)
	private UUID eventId;

	public Notification(UUID receiverId, String title, String content, NotificationLevel level) {
		this.receiverId = receiverId;
		this.title = title;
		this.content = content;
		this.level = level;
	}
}
