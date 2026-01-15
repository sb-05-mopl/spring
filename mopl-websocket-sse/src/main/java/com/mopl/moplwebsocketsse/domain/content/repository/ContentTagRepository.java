package com.mopl.moplwebsocketsse.domain.content.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mopl.moplwebsocketsse.domain.content.entity.ContentTag;
import com.mopl.moplwebsocketsse.domain.content.entity.Tag;

public interface ContentTagRepository extends JpaRepository<ContentTag, UUID> {
	@Query("SELECT t FROM ContentTag ct JOIN ct.tag t WHERE ct.content.id = :contentId")
	List<Tag> findTagsByContentId(UUID contentId);

	@Query("""
    SELECT ct FROM ContentTag ct
    JOIN FETCH ct.tag
    WHERE ct.content.id IN :contentIds
    """)
	List<ContentTag> findByContentIds(List<UUID> contentIds);
}
