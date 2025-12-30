package com.mopl.moplcore.domain.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.content.entity.ContentTag;

public interface ContentTagRepository extends JpaRepository<ContentTag, UUID> {
	List<ContentTag> findByContentId(UUID contentId);
}
