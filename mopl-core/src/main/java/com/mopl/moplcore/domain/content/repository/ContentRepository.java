package com.mopl.moplcore.domain.content.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.content.entity.Content;

public interface ContentRepository extends JpaRepository<Content, UUID> {
}
