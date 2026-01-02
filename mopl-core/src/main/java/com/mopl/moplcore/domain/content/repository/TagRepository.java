package com.mopl.moplcore.domain.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.content.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, UUID> {
	Optional<Tag> findByName(String tagName);
}
