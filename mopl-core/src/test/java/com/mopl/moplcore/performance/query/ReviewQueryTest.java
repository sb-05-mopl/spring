package com.mopl.moplcore.performance.query;

import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.Type;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.review.dto.ReviewSearchRequest;
import com.mopl.moplcore.domain.review.dto.ReviewSortBy;
import com.mopl.moplcore.domain.review.dto.ReviewSortDirection;
import com.mopl.moplcore.domain.review.entity.Review;
import com.mopl.moplcore.domain.review.repository.ReviewRepository;
import com.mopl.moplcore.domain.review.service.ReviewService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest
@ActiveProfiles({"dev", "test"})
@Transactional
class ReviewQueryTest {
    //
    // @Autowired
    // QueryCounter queryCounter;
    //
    // @Autowired
    // ReviewService reviewService;
    //
    // @Autowired
    // UserRepository userRepository;
    //
    // @Autowired
    // ContentRepository contentRepository;
    //
    // @Autowired
    // ReviewRepository reviewRepository;
    //
    // @Autowired
    // EntityManager entityManager;
    //
    // private UUID contentId;
    // private static final int REVIEW_COUNT = 10;
    //
    // @BeforeEach
    // void setUp(TestInfo testInfo) {
    //     if (testInfo.getDisplayName().contains("연결")) {
    //         return;
    //     }
    //
    //     List<User> authors = new ArrayList<>();
    //     for (int i = 0; i < REVIEW_COUNT; i++) {
    //         User author = new User(
    //             "테스터" + i,
    //             "tester" + i + "@test.com",
    //             "password123"
    //         );
    //         authors.add(userRepository.save(author));
    //     }
    //
    //     Content content = Content.builder()
    //         .title("테스트 영화")
    //         .description("테스트용 영화입니다.")
    //         .type(Type.MOVIE)
    //         .sourceId(999999L)
    //         .build();
    //     content = contentRepository.save(content);
    //     contentId = content.getId();
    //
    //     for (int i = 0; i < REVIEW_COUNT; i++) {
    //         Review review = Review.builder()
    //             .author(authors.get(i))
    //             .content(content)
    //             .text("리뷰 내용 " + i)
    //             .rating(3.0 + (i % 3))
    //             .build();
    //         reviewRepository.save(review);
    //     }
    //
    //     entityManager.flush();
    //     entityManager.clear();
    // }
    //
    // @Test
    // @DisplayName("[Review] 목록 조회 - limit 10")
    // void 리뷰_목록_조회_limit10() {
    //     // given
    //     int limit = 10;
    //     int expectedQueries = 2;  // count 1 + select 1
    //
    //     ReviewSearchRequest request = new ReviewSearchRequest(
    //         contentId, null, null, limit,
    //         ReviewSortDirection.DESCENDING,
    //         ReviewSortBy.createdAt
    //     );
    //
    //     // when
    //     long actual = queryCounter.count(() -> {
    //         reviewService.findReviews(request);
    //     });
    //
    //     // then
    //     printResult("GET /api/reviews", limit, expectedQueries, actual);
    //     assertThat(actual).isLessThanOrEqualTo(expectedQueries);
    // }
    //
    // @Test
    // @DisplayName("[Review] 목록 조회 - limit 5")
    // void 리뷰_목록_조회_limit5() {
    //     // given
    //     int limit = 5;
    //     int expectedQueries = 2;
    //
    //     ReviewSearchRequest request = new ReviewSearchRequest(
    //         contentId, null, null, limit,
    //         ReviewSortDirection.DESCENDING,
    //         ReviewSortBy.createdAt
    //     );
    //
    //     // when
    //     long actual = queryCounter.count(() -> {
    //         reviewService.findReviews(request);
    //     });
    //
    //     // then
    //     printResult("GET /api/reviews", limit, expectedQueries, actual);
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
    // private void printResult(String api, int limit, int expected, long actual) {
    //     String status = actual <= expected ? "정상" : "N+1 의심";
    //     String nPlusOne = actual > expected ? " (+" + (actual - expected) + " 추가 쿼리)" : "";
    //
    //     System.out.println();
    //     System.out.println("==============================");
    //     System.out.println(api);
    //     System.out.println("==============================");
    //     System.out.println("limit   : " + limit);
    //     System.out.println("예상 쿼리 : " + expected);
    //     System.out.println("실제 쿼리 : " + actual + nPlusOne);
    //     System.out.println("결과     : " + status);
    //     System.out.println("==============================");
    //     System.out.println();
    // }
}
