package com.mopl.moplwebsocketsse.security.principal;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.user.dto.UserDto;
import com.mopl.moplwebsocketsse.domain.user.entity.User;
import com.mopl.moplwebsocketsse.domain.user.exception.UserNotFoundException;
import com.mopl.moplwebsocketsse.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoplUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Transactional
	@Override
	public UserDetails loadUserByUsername(String email) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> UserNotFoundException.withEmail(email));
		UserDto dto = new UserDto(
			user.getId(),
			user.getCreatedAt(),
			user.getEmail(),
			user.getName(),
			user.getProfileImageUrl(),
			user.getRole(),
			user.isLocked(),
			user.getProvider()
		);
		return new MoplUserDetails(dto, user.getPassword());
	}
}
