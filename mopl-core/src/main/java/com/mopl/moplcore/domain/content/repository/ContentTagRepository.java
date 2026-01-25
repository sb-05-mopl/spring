package com.mopl.moplcore.domain.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mopl.moplcore.domain.content.entity.ContentTag;

public interface ContentTagRepository extends JpaRepository<ContentTag, UUID> {
	@Query("SELECT ct FROM ContentTag ct JOIN FETCH ct.tag WHERE ct.content.id = :contentId")
	List<ContentTag> findByContentId(UUID contentId);

	List<ContentTag> findByContentIdIn(List<UUID> contentIds);

	void deleteByContentId(UUID id);

	@Query("""
    SELECT ct FROM ContentTag ct
    JOIN FETCH ct.tag
    WHERE ct.content.id IN :contentIds
    """)
	List<ContentTag> findByContentIds(List<UUID> contentIds);
}
