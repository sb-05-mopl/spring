package com.mopl.moplcore.domain.content.repository;

import java.util.List;

import com.mopl.moplcore.domain.content.dto.ContentSearchRequest;
import com.mopl.moplcore.domain.content.entity.Content;

public interface ContentRepositoryCustom {
	List<Content> searchContents(ContentSearchRequest request);
	Long countContents(ContentSearchRequest request);
}
