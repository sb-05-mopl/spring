package com.mopl.moplcore.global.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
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
			.withConnectTimeout(Duration.ofSeconds(10))
			.withSocketTimeout(Duration.ofSeconds(60))
			.withClientConfigurer(
				ElasticsearchClients.ElasticsearchHttpClientConfigurationCallback.from(httpClientBuilder ->
					httpClientBuilder
						.setMaxConnTotal(100)
						.setMaxConnPerRoute(100)
				)
			)
			.build();
	}
}