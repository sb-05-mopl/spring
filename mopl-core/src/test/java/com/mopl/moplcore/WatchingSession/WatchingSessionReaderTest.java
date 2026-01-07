package com.mopl.moplcore.WatchingSession;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.mopl.moplcore.domain.watch.repository.WatchingSessionReader;

@SpringBootTest
@ActiveProfiles("dev")
class WatchingSessionReaderTest {

	@Autowired
	private WatchingSessionReader reader;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Test
	void countByContentId_동작확인() {
		// given
		UUID contentId = UUID.randomUUID();
		String key = "watch:content:" + contentId;

		redisTemplate.opsForZSet().add(key, "user1", System.currentTimeMillis());
		redisTemplate.opsForZSet().add(key, "user2", System.currentTimeMillis());

		// when
		long count = reader.countByContentId(contentId);

		// then
		assertThat(count).isEqualTo(2);

		// cleanup
		redisTemplate.delete(key);
	}
}