package com.mopl.moplcore.domain.user.service;

import com.mopl.moplcore.domain.user.dto.AdminUserSearchRequest;
import com.mopl.moplcore.domain.user.dto.CursorResponseUserDto;
import com.mopl.moplcore.domain.user.dto.SortBy;
import com.mopl.moplcore.domain.user.dto.UserCreateRequest;
import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.dto.UserUpdateRequest;
import com.mopl.moplcore.domain.user.entity.Role;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.exception.DuplicateEmailException;
import com.mopl.moplcore.domain.user.exception.ForbiddenUserAccessException;
import com.mopl.moplcore.domain.user.exception.UserNotFoundException;
import com.mopl.moplcore.domain.user.mapper.UserMapper;
import com.mopl.moplcore.domain.user.repository.UserRepository;
import com.mopl.moplcore.domain.user.storage.ProfileImageStorage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final ProfileImageStorage profileImageStorage;

	@Transactional
	public UserDto signUp(UserCreateRequest dto) {

		if (userRepository.existsByEmail(dto.getEmail())) {
			throw DuplicateEmailException.withEmail(dto.getEmail());
		}

		String encodedPassword = passwordEncoder.encode(dto.getPassword());
		User user = new User(dto.getName(), dto.getEmail(), encodedPassword);

		User saved = userRepository.save(user);
		return userMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public UserDto findById(UUID userId) {
		return userRepository.findById(userId)
			.map(userMapper::toDto)
			.orElseThrow(() -> UserNotFoundException.withUserId(userId));
	}

	@Transactional(readOnly = true)
	public CursorResponseUserDto findUsers(AdminUserSearchRequest req) {

		long totalCount = userRepository.countUsers(req.emailLike(), req.roleEqual(), req.isLocked());

		List<User> rowsPlusOne = userRepository.findUsersWithCursor(
			req.emailLike(),
			req.roleEqual(),
			req.isLocked(),
			req.cursor(),
			req.idAfter(),
			req.limit(),
			req.sortBy(),
			req.sortDirection()
		);

		boolean hasNext = rowsPlusOne.size() > req.limit();
		List<User> rows = hasNext ? rowsPlusOne.subList(0, req.limit()) : rowsPlusOne;

		List<UserDto> data = rows.stream()
			.map(userMapper::toDto)
			.toList();

		if (rows.isEmpty()) {
			return new CursorResponseUserDto(
				data,
				null,
				null,
				false,
				totalCount,
				req.sortBy().name(),
				req.sortDirection().name()
			);
		}

		User last = rows.get(rows.size() - 1);

		String nextCursor = hasNext ? toNextCursor(last, req.sortBy()) : null;
		UUID nextIdAfter = hasNext ? last.getId() : null;

		return new CursorResponseUserDto(
			data,
			nextCursor,
			nextIdAfter,
			hasNext,
			totalCount,
			req.sortBy().name(),
			req.sortDirection().name()
		);
	}

	@Transactional
	public void updateRole(Role newRole, UUID userId){
		User user = userRepository.findById(userId).orElseThrow(() -> UserNotFoundException.withUserId(userId));
		user.updateRole(newRole);
	}

	@Transactional
	public void updateLock(boolean locked, UUID userId){
		User user = userRepository.findById(userId).orElseThrow(() -> UserNotFoundException.withUserId(userId));
		if (locked) {
			user.lockUser();
		} else {
			user.unlockUser();
		}
	}





	@Transactional
	public UserDto updateProfile(
		UUID pathUserId,
		UUID loginUserId,
		UserUpdateRequest request,
		MultipartFile image
	) {
		log.info("pathUserId={}, loginUserId={}", pathUserId, loginUserId);

		if (!pathUserId.equals(loginUserId)) {
			throw ForbiddenUserAccessException.withIds(loginUserId, pathUserId);
		}

		return userRepository.findById(pathUserId)
			.map(user -> {
				user.updateName(request.name());

				Optional.ofNullable(image)
					.filter(file -> !file.isEmpty())
					.map(file -> profileImageStorage.saveProfileImage(pathUserId, file))
					.map(profileImageStorage::getProfileImageUrl)
					.ifPresent(user::updateProfileImageUrl);

				return user;
			})
			.map(userMapper::toDto)
			.orElseThrow(() -> UserNotFoundException.withUserId(pathUserId));
	}

	private String toNextCursor(User last, SortBy sortBy) {
		return switch (sortBy) {
			case name -> last.getName();
			case email -> last.getEmail();
			case role -> last.getRole().name();
			case isLocked -> String.valueOf(last.isLocked());
			case createdAt -> last.getCreatedAt().toString();
		};
	}
}
