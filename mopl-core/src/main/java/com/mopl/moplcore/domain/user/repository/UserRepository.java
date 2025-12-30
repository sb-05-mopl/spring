package com.mopl.moplcore.domain.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
}
