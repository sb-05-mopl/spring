package com.mopl.moplcore.domain.user.service;

import com.mopl.moplcore.domain.user.dto.UserCreateRequest;
import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.exception.DuplicateEmailException;
import com.mopl.moplcore.domain.user.exception.UserNotFoundException;
import com.mopl.moplcore.domain.user.mapper.UserMapper;
import com.mopl.moplcore.domain.user.repository.UserRepository;
import java.util.Optional;
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
}
