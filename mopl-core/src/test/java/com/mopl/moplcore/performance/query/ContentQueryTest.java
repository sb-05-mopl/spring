package com.mopl.moplcore.performance.query;

import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.ContentTag;
import com.mopl.moplcore.domain.content.entity.Tag;
import com.mopl.moplcore.domain.content.entity.Type;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentTagRepository;
import com.mopl.moplcore.domain.content.repository.TagRepository;
import com.mopl.moplcore.domain.content.service.ContentSearchService;
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

@SpringBootTest
@ActiveProfiles({"dev", "test"})
@Transactional
class ContentQueryTest {

    @Autowired
    QueryCounter queryCounter;

    @Autowired
    ContentSearchService contentSearchService;

    @Autowired
    ContentRepository contentRepository;

    @Autowired
    ContentTagRepository contentTagRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    EntityManager entityManager;

    private UUID contentId;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        if (testInfo.getDisplayName().contains("연결")) {
            return;
        }

        // 태그 생성 (테스트용 고유 이름)
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        Tag tag1 = tagRepository.save(new Tag("test_tag_1_" + uniqueSuffix));
        Tag tag2 = tagRepository.save(new Tag("test_tag_2_" + uniqueSuffix));

        // 콘텐츠 생성
        Content content = Content.builder()
            .title("테스트 영화")
            .description("테스트용 영화입니다.")
            .type(Type.MOVIE)
            .sourceId(System.nanoTime())
            .build();
        content = contentRepository.save(content);
        contentId = content.getId();

        // 태그 연결
        contentTagRepository.save(new ContentTag(content, tag1));
        contentTagRepository.save(new ContentTag(content, tag2));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("[Content] 단건 조회")
    void 콘텐츠_단건_조회() {
        // given
        // content 조회 1 + tags 조회 1 = 2
        int expectedQueries = 2;

        // when
        long actual = queryCounter.count(() -> {
            contentSearchService.getContent(contentId);
        });

        // then
        printResult("GET /api/contents/{id}", expectedQueries, actual);
        assertThat(actual).isLessThanOrEqualTo(expectedQueries);
    }

    @Test
    @DisplayName("연결 테스트")
    void 연결_테스트() {
        long count = queryCounter.count(() -> {});
        System.out.println("연결 성공 - 쿼리 수: " + count);
        assertThat(count).isEqualTo(0);
    }

    private void printResult(String api, int expected, long actual) {
        String status = actual <= expected ? "정상" : "N+1 의심";
        String nPlusOne = actual > expected ? " (+" + (actual - expected) + " 추가 쿼리)" : "";

        System.out.println();
        System.out.println("==============================");
        System.out.println(api);
        System.out.println("==============================");
        System.out.println("예상 쿼리 : " + expected);
        System.out.println("실제 쿼리 : " + actual + nPlusOne);
        System.out.println("결과     : " + status);
        System.out.println("==============================");
        System.out.println();
    }
}
