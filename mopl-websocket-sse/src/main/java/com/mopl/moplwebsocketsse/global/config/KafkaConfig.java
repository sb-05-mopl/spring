package com.mopl.moplwebsocketsse.global.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
		FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3); // 1초 간격 3회 재시도

		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
			kafkaTemplate, (record, ex)
			-> new TopicPartition(record.topic() + ".dlq", record.partition())
		);

		return new DefaultErrorHandler(recoverer, fixedBackOff);
	}
}
