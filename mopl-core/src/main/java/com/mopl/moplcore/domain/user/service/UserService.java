package com.mopl.moplcore.domain.user.service;

import com.mopl.moplcore.domain.user.dto.AdminUserSearchRequest;
import com.mopl.moplcore.domain.user.dto.CursorResponseUserDto;
import com.mopl.moplcore.domain.user.dto.SortBy;
import com.mopl.moplcore.domain.user.dto.UserCreateRequest;
import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.exception.DuplicateEmailException;
import com.mopl.moplcore.domain.user.exception.UserNotFoundException;
import com.mopl.moplcore.domain.user.mapper.UserMapper;
import com.mopl.moplcore.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Transactional
  public UserDto signUp(UserCreateRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw DuplicateEmailException.withEmail(request.email());
    }
    String encodedPassword = passwordEncoder.encode(request.password());
    User user = new User(request.name(), request.email(), encodedPassword);

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
