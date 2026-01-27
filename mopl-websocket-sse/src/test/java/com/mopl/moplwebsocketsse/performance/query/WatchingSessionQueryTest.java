package com.mopl.moplwebsocketsse.performance.query;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;
import com.mopl.moplwebsocketsse.domain.content.entity.Content;
import com.mopl.moplwebsocketsse.domain.content.entity.ContentTag;
import com.mopl.moplwebsocketsse.domain.content.entity.Tag;
import com.mopl.moplwebsocketsse.domain.content.entity.Type;
import com.mopl.moplwebsocketsse.domain.content.repository.ContentRepository;
import com.mopl.moplwebsocketsse.domain.content.repository.ContentTagRepository;
import com.mopl.moplwebsocketsse.domain.content.repository.TagRepository;
import com.mopl.moplwebsocketsse.domain.user.entity.User;
import com.mopl.moplwebsocketsse.domain.user.repository.UserRepository;
import com.mopl.moplwebsocketsse.domain.watch.dto.CursorResponseWatchingSessionDto;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionSearchRequest;
import com.mopl.moplwebsocketsse.domain.watch.dto.WatchingSessionSortBy;
import com.mopl.moplwebsocketsse.domain.watch.entity.WatchingSession;
import com.mopl.moplwebsocketsse.domain.watch.repository.WatchingSessionRepository;
import com.mopl.moplwebsocketsse.domain.watch.service.WatchingSessionService;
import com.mopl.moplwebsocketsse.util.QueryCounter;

import jakarta.persistence.EntityManager;

// @SpringBootTest
// @ActiveProfiles("test")
// @Transactional
class WatchingSessionQueryTest {
    //
    // @Autowired
    // private WatchingSessionService watchingSessionService;
    //
    // @Autowired
    // private WatchingSessionRepository watchingSessionRepository;
    //
    // @Autowired
    // private UserRepository userRepository;
    //
    // @Autowired
    // private ContentRepository contentRepository;
    //
    // @Autowired
    // private ContentTagRepository contentTagRepository;
    //
    // @Autowired
    // private TagRepository tagRepository;
    //
    // @Autowired
    // private QueryCounter queryCounter;
    //
    // @Autowired
    // private EntityManager em;
    //
    // private Content testContent;
    // private List<User> testUsers;
    // private static final int USER_COUNT = 5;
    //
    // @BeforeEach
    // void setUp() {
    //     String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    //
    //     // 1. 테스트용 Content 생성
    //     testContent = Content.builder()
    //         .type(Type.MOVIE)
    //         .title("테스트 영화 " + uniqueId)
    //         .description("테스트 설명")
    //         .sourceId(System.currentTimeMillis())
    //         .build();
    //     contentRepository.save(testContent);
    //
    //     // 2. 테스트용 Tag 생성 및 연결 (고유한 이름!)
    //     Tag tag1 = new Tag("액션_" + uniqueId);
    //     Tag tag2 = new Tag("SF_" + uniqueId);
    //     tagRepository.save(tag1);
    //     tagRepository.save(tag2);
    //
    //     contentTagRepository.save(new ContentTag(testContent, tag1));
    //     contentTagRepository.save(new ContentTag(testContent, tag2));
    //
    //     // 3. 테스트용 User 생성 (고유한 email!)
    //     testUsers = new ArrayList<>();
    //     for (int i = 0; i < USER_COUNT; i++) {
    //         User user = new User(
    //             "테스트유저" + i,
    //             "test_" + uniqueId + "_" + i + "@test.com",
    //             "password"
    //         );
    //         userRepository.save(user);
    //         testUsers.add(user);
    //     }
    //
    //     // 4. Redis에 WatchingSession 저장
    //     for (User user : testUsers) {
    //         WatchingSession session = WatchingSession.builder()
    //             .id(UUID.randomUUID())
    //             .watcherId(user.getId())
    //             .contentId(testContent.getId())
    //             .createdAt(Instant.now())
    //             .build();
    //         watchingSessionRepository.save(session);
    //     }
    //
    //     em.flush();
    //     em.clear();
    // }
    //
    // @Test
    // @DisplayName("GET /api/contents/{contentId}/watching-sessions - N+1 쿼리 테스트")
    // void findSessionsByContent_N1QueryTest() {
    //     // given
    //     int limit = 5;
    //     WatchingSessionSearchRequest request = new WatchingSessionSearchRequest(
    //         null,
    //         null,
    //         null,
    //         limit,
    //         SortDirection.DESCENDING,
    //         WatchingSessionSortBy.createdAt
    //     );
    //
    //     // 수정 전 예상: User N번 + Content 1번 + Tag 1번 = 7번+
    //     // 수정 후 예상: User 1번(배치) + Content 1번 + Tag 1번 = 3번
    //     int expectedQueries = 3;
    //
    //     // when
    //     long queryCount = queryCounter.count(() -> {
    //         CursorResponseWatchingSessionDto result =
    //             watchingSessionService.findSessionsByContent(testContent.getId(), request);
    //
    //         assertThat(result.data()).hasSize(USER_COUNT);
    //     });
    //
    //     // then
    //     System.out.println("==============================");
    //     System.out.println("GET /api/contents/{contentId}/watching-sessions");
    //     System.out.println("==============================");
    //     System.out.println("limit   : " + limit);
    //     System.out.println("예상 쿼리 : " + expectedQueries);
    //     System.out.println("실제 쿼리 : " + queryCount);
    //
    //     if (queryCount > expectedQueries) {
    //         System.out.println("결과     : N+1 의심 (+" + (queryCount - expectedQueries) + " 추가 쿼리)");
    //     } else {
    //         System.out.println("결과     : 정상");
    //     }
    //     System.out.println("==============================");
    //
    //     assertThat(queryCount).isLessThanOrEqualTo(expectedQueries);
    // }
    //
    // @Test
    // @DisplayName("GET /api/users/{watcherId}/watching-sessions - 단건 조회 테스트")
    // void findSessionByWatcher_QueryTest() {
    //     // given
    //     User testUser = testUsers.get(0);
    //
    //     // 예상: User 1번 + Content 1번 + Tag 1번 = 3번
    //     int expectedQueries = 3;
    //
    //     // when
    //     long queryCount = queryCounter.count(() -> {
    //         var result = watchingSessionService.findSessionByWatcher(testUser.getId());
    //         assertThat(result).isNotNull();
    //     });
    //
    //     // then
    //     System.out.println("==============================");
    //     System.out.println("GET /api/users/{watcherId}/watching-sessions");
    //     System.out.println("==============================");
    //     System.out.println("예상 쿼리 : " + expectedQueries);
    //     System.out.println("실제 쿼리 : " + queryCount);
    //
    //     if (queryCount > expectedQueries) {
    //         System.out.println("결과     : 추가 쿼리 발생 (+" + (queryCount - expectedQueries) + ")");
    //     } else {
    //         System.out.println("결과     : 정상");
    //     }
    //     System.out.println("==============================");
    //
    //     assertThat(queryCount).isLessThanOrEqualTo(expectedQueries);
    // }
}
