package com.mopl.moplcore.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.mopl.moplcore.domain.content.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

	@Value("${spring.elasticsearch.uris}")
	private String elasticsearchUri;

	@Override
	public ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder()
			.connectedTo(elasticsearchUri.replace("http://", ""))
			.build();
	}
}