package com.mopl.moplwebsocketsse.domain.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplwebsocketsse.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
}
