package com.mopl.moplcore.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

@Deprecated
@Profile("prod")
@Configuration
public class AwsSesConfig {
	@Value("${cloud.aws.creadentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.creadentials.secret-key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Bean
	public AmazonSimpleEmailService amazonSimpleEmailService() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		return AmazonSimpleEmailServiceClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withRegion(region)
			.build();
	}
}
