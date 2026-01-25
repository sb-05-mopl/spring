package com.mopl.moplcore.performance.query;

import com.mopl.moplcore.domain.follow.repository.FollowRepository;
import com.mopl.moplcore.domain.follow.service.FollowService;
import com.mopl.moplcore.domain.user.entity.Follow;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.repository.UserRepository;
import com.mopl.moplcore.util.QueryCounter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest
@ActiveProfiles({"dev", "test"})
@Transactional
class FollowQueryTest {
    //
    // @Autowired
    // QueryCounter queryCounter;
    //
    // @Autowired
    // FollowService followService;
    //
    // @Autowired
    // UserRepository userRepository;
    //
    // @Autowired
    // FollowRepository followRepository;
    //
    // @Autowired
    // EntityManager entityManager;
    //
    // private UUID followerId;
    // private UUID followeeId;
    //
    // @BeforeEach
    // void setUp(TestInfo testInfo) {
    //     if (testInfo.getDisplayName().contains("연결")) {
    //         return;
    //     }
    //
    //     // 팔로워 (나)
    //     User follower = new User(
    //         "팔로워",
    //         "follower@test.com",
    //         "password123"
    //     );
    //     follower = userRepository.save(follower);
    //     followerId = follower.getId();
    //
    //     // 팔로이 (상대방)
    //     User followee = new User(
    //         "팔로이",
    //         "followee@test.com",
    //         "password123"
    //     );
    //     followee = userRepository.save(followee);
    //     followeeId = followee.getId();
    //
    //     // 팔로우 관계 생성
    //     Follow follow = new Follow(follower, followee);
    //     followRepository.save(follow);
    //
    //     entityManager.flush();
    //     entityManager.clear();
    // }
    //
    // @Test
    // @DisplayName("[Follow] 팔로워 수 조회")
    // void 팔로워_수_조회() {
    //     // given
    //     int expectedQueries = 2;  // user 존재 확인 1 + count 1
    //
    //     // when
    //     long actual = queryCounter.count(() -> {
    //         followService.countFollowers(followeeId);
    //     });
    //
    //     // then
    //     printResult("GET /api/follows/count", expectedQueries, actual);
    //     assertThat(actual).isLessThanOrEqualTo(expectedQueries);
    // }
    //
    // @Test
    // @DisplayName("[Follow] 팔로우 여부 조회")
    // void 팔로우_여부_조회() {
    //     // given
    //     int expectedQueries = 3;  // follower 확인 1 + followee 확인 1 + exists 1
    //
    //     // when
    //     long actual = queryCounter.count(() -> {
    //         followService.isFollowedByMe(followerId, followeeId);
    //     });
    //
    //     // then
    //     printResult("GET /api/follows/followed-by-me", expectedQueries, actual);
    //     assertThat(actual).isLessThanOrEqualTo(expectedQueries);
    // }
    //
    // @Test
    // @DisplayName("연결 테스트")
    // void 연결_테스트() {
    //     long count = queryCounter.count(() -> {});
    //     System.out.println("연결 성공 - 쿼리 수: " + count);
    //     assertThat(count).isEqualTo(0);
    // }
    //
    // private void printResult(String api, int expected, long actual) {
    //     String status = actual <= expected ? "정상" : "N+1 의심";
    //     String extra = actual > expected ? " (+" + (actual - expected) + " 추가 쿼리)" : "";
    //
    //     System.out.println();
    //     System.out.println("==============================");
    //     System.out.println(api);
    //     System.out.println("==============================");
    //     System.out.println("예상 쿼리 : " + expected);
    //     System.out.println("실제 쿼리 : " + actual + extra);
    //     System.out.println("결과     : " + status);
    //     System.out.println("==============================");
    //     System.out.println();
    // }
}
