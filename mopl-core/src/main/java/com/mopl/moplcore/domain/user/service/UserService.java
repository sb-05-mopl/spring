package com.mopl.moplcore.domain.user.service;

import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.User;
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

  @Transactional
  public User signUp(String name, String email, String password) {
    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already exists");
    }
    String encodedPassword = passwordEncoder.encode(password);
    User user = new User(name, email, encodedPassword);
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public UserDto findById(UUID userId) {
    Optional<User> user = userRepository.findById(userId);
    return new UserDto(
        user.get().getId(),
        user.get().getCreatedAt(),
        user.get().getEmail(),
        user.get().getName(),
        user.get().getProfileImageUrl(),
        user.get().getRole(),
        user.get().isLocked()
    );
  }
}
