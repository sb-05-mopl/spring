package com.mopl.mopl_spring.domain.user.entity;

import com.mopl.mopl_spring.domain.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	private boolean locked;

	public User(String name, String email, String password) {
		this.name = name;
		this.password = password;
		this.email = email;
		this.role = Role.USER;
		this.locked = false;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void updateProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public void lockUser() {
		this.locked = true;
	}

	public void unlockUser() {
		this.locked = false;
	}

	public void updateRole(Role role) {
		this.role = role;
	}
}
