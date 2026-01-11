package com.mopl.moplcore.security.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplcore.domain.user.entity.AuthProvider;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InitService {

	@Value("${init.admin.password}")
	private String adminPassword;

	@Value("${init.admin.email}")
	private String adminEmail;

	@Value("${init.user.password}")
	private String userPassword;

	@Value("${init.user.email}")
	private String userEmail;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void initAdmin() {
		if (userRepository.existsByEmailAndProvider(adminEmail, AuthProvider.LOCAL)) {
			return;
		}

		String encodedPassword = passwordEncoder.encode(adminPassword);
		User admin = new User("관리자", adminEmail, encodedPassword);
		admin.initAdmin();

		userRepository.save(admin);
	}

	@Transactional
	public void initDefaultUser() {
		if (userRepository.existsByEmailAndProvider(userEmail, AuthProvider.LOCAL)) {
			return;
		}

		String encodedPassword = passwordEncoder.encode(userPassword);
		User user = new User("테스트 사용자", userEmail, encodedPassword);

		userRepository.save(user);
	}

}
