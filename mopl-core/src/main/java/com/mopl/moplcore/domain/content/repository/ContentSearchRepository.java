package com.mopl.moplcore.domain.content.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.mopl.moplcore.domain.content.document.ContentDocument;

@Repository
public interface ContentSearchRepository extends ElasticsearchRepository<ContentDocument, String> {
}