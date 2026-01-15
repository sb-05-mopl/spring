package com.mopl.moplwebsocketsse.domain.content.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplwebsocketsse.domain.content.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, UUID> {
}
