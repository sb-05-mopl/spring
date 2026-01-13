package com.mopl.moplwebsocketsse.conversation;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;
import com.mopl.moplwebsocketsse.domain.directMessage.exception.ConversationLockAcquisitionFailedException;
import com.mopl.moplwebsocketsse.domain.directMessage.repository.ConversationParticipantsRepository;
import com.mopl.moplwebsocketsse.domain.directMessage.repository.ConversationRepository;
import com.mopl.moplwebsocketsse.domain.directMessage.service.ConversationService;
import com.mopl.moplwebsocketsse.domain.user.entity.User;
import com.mopl.moplwebsocketsse.domain.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class ConversationDistributedLockTest {

	@Autowired
	private ConversationService conversationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ConversationRepository conversationRepository;

	@Autowired
	private ConversationParticipantsRepository participantsRepository;

	private UUID userA;
	private UUID userB;

	@BeforeEach
	void setUp() {
		User testUserA = new User("테스트A", "testA_" + UUID.randomUUID() + "@test.com", "password123");
		User testUserB = new User("테스트B", "testB_" + UUID.randomUUID() + "@test.com", "password123");

		userRepository.save(testUserA);
		userRepository.save(testUserB);

		userA = testUserA.getId();
		userB = testUserB.getId();
	}

	@AfterEach
	void tearDown() {
		Optional<Conversation> conversation = participantsRepository.findConversationBetween(userA, userB);
		if (conversation.isPresent()) {
			UUID convId = conversation.get().getId();
			participantsRepository.deleteByConversationId(convId);
			conversationRepository.deleteById(convId);
		}

		userRepository.deleteById(userA);
		userRepository.deleteById(userB);
	}

	@Test
	@DisplayName("동시에 같은 대화방 생성 요청 시 락으로 인해 하나만 생성된다")
	void 동시_대화방_생성시_분산락_동작_확인() throws InterruptedException {
		// given
		int threadCount = 10;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch readyLatch = new CountDownLatch(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger lockFailCount = new AtomicInteger(0);

		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					readyLatch.countDown();
					startLatch.await();

					conversationService.createConversation(userA, userB);
					successCount.incrementAndGet();

				} catch (ConversationLockAcquisitionFailedException e) {
					lockFailCount.incrementAndGet();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					System.out.println("기타 에러: " + e.getClass().getSimpleName() + " - " + e.getMessage());
				}
			});
		}

		readyLatch.await();
		startLatch.countDown();

		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);

		// then
		System.out.println("=== 테스트 결과 ===");
		System.out.println("성공: " + successCount.get());
		System.out.println("락 실패: " + lockFailCount.get());

		// 1. 대화방은 1개만 존재해야 함
		Optional<Conversation> conversation = participantsRepository.findConversationBetween(userA, userB);
		assertThat(conversation).isPresent();

		// 2. 락 실패가 발생해야 함 (동시성 충돌 증명)
		assertThat(lockFailCount.get()).isEqualTo(9);

		// 3. 성공 + 락 실패 = 전체 스레드 수
		assertThat(successCount.get() + lockFailCount.get()).isEqualTo(threadCount);
	}
}