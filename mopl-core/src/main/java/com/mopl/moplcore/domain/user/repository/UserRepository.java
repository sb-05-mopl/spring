package com.mopl.moplcore.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

}
