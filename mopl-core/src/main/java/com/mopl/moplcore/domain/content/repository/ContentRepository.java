package com.mopl.moplcore.domain.content.repository;

import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mopl.moplcore.domain.content.entity.Content;

public interface ContentRepository extends JpaRepository<Content, UUID>, ContentRepositoryCustom {
	@EntityGraph(attributePaths = {"contentTags", "contentTags.tag"})
	@Query("select c from Content c")
	Page<Content> findAllForSync(Pageable pageable);
}
