package com.mopl.moplcore.security;

import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MoplUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Transactional
  @Override
  public UserDetails loadUserByUsername(String emailOrUserId) throws UsernameNotFoundException {
    // jwt 인증에서는 userId를 username 자리에 전달
    User user;

    try {
      UUID userId = UUID.fromString(emailOrUserId);
      user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException(emailOrUserId));
    } catch (IllegalArgumentException notUuid) {
      user = userRepository.findByEmail(emailOrUserId).orElseThrow(() -> new UsernameNotFoundException(emailOrUserId));
    }

    UserDto dto = new UserDto(
        user.getId(),
        user.getCreatedAt(),
        user.getEmail(),
        user.getName(),
        user.getProfileImageUrl(),
        user.getRole(),
        user.isLocked()
    );

    return new MoplUserDetails(dto, user.getPassword());
  }
}
