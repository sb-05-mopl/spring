package com.mopl.mopl_spring.domain.notification.entity;

import java.time.Instant;

import com.mopl.mopl_spring.domain.common.entity.BaseEntity;
import com.mopl.mopl_spring.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "notifications")
public class Notification extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private User receiver;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationLevel level;

	@Column(name = "read_at")
	private Instant readAt;

	public  Notification(User receiver, String title, String content, NotificationLevel level) {
		this.receiver = receiver;
		this.title = title;
		this.content = content;
		this.level = level;
		this.readAt = null;
	}

	public boolean isRead() {
		return readAt != null;
	}

	public void markAsRead() {
		this.readAt = Instant.now();
	}
}
