package com.mopl.moplcore.performance.query;

import com.mopl.moplcore.domain.content.entity.Content;
import com.mopl.moplcore.domain.content.entity.ContentTag;
import com.mopl.moplcore.domain.content.entity.Tag;
import com.mopl.moplcore.domain.content.entity.Type;
import com.mopl.moplcore.domain.content.repository.ContentRepository;
import com.mopl.moplcore.domain.content.repository.ContentTagRepository;
import com.mopl.moplcore.domain.content.repository.TagRepository;
import com.mopl.moplcore.domain.playlist.dto.PlaylistSearchRequest;
import com.mopl.moplcore.domain.playlist.entity.Playlist;
import com.mopl.moplcore.domain.playlist.entity.PlaylistContent;
import com.mopl.moplcore.domain.playlist.repository.PlaylistContentRepository;
import com.mopl.moplcore.domain.playlist.repository.PlaylistRepository;
import com.mopl.moplcore.domain.playlist.service.PlaylistService;
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
class PlaylistQueryTest {

    // @Autowired
    // QueryCounter queryCounter;
    //
    // @Autowired
    // PlaylistService playlistService;
    //
    // @Autowired
    // PlaylistRepository playlistRepository;
    //
    // @Autowired
    // PlaylistContentRepository playlistContentRepository;
    //
    // @Autowired
    // ContentRepository contentRepository;
    //
    // @Autowired
    // ContentTagRepository contentTagRepository;
    //
    // @Autowired
    // TagRepository tagRepository;
    //
    // @Autowired
    // UserRepository userRepository;
    //
    // @Autowired
    // EntityManager entityManager;
    //
    // private UUID userId;
    // private UUID playlistId;
    // private static final int PLAYLIST_COUNT = 5;
    // private static final int CONTENT_PER_PLAYLIST = 2;
    //
    // @BeforeEach
    // void setUp(TestInfo testInfo) {
    //     if (testInfo.getDisplayName().contains("연결")) {
    //         return;
    //     }
    //
    //     // 태그 생성 (테스트용 고유 이름)
    //     String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
    //     Tag tag1 = tagRepository.save(new Tag("test_tag_1_" + uniqueSuffix));
    //     Tag tag2 = tagRepository.save(new Tag("test_tag_2_" + uniqueSuffix));
    //
    //     // 사용자들 생성
    //     List<User> owners = new ArrayList<>();
    //     for (int i = 0; i < PLAYLIST_COUNT; i++) {
    //         User owner = new User(
    //             "소유자" + i,
    //             "owner" + i + "_" + uniqueSuffix + "@test.com",
    //             "password123"
    //         );
    //         owners.add(userRepository.save(owner));
    //     }
    //     userId = owners.get(0).getId();
    //
    //     // 플레이리스트와 콘텐츠 생성
    //     for (int i = 0; i < PLAYLIST_COUNT; i++) {
    //         Playlist playlist = new Playlist(owners.get(i), "플레이리스트" + i, "설명" + i);
    //         playlist = playlistRepository.save(playlist);
    //
    //         if (i == 0) {
    //             playlistId = playlist.getId();
    //         }
    //
    //         // 각 플레이리스트에 콘텐츠 추가
    //         for (int j = 0; j < CONTENT_PER_PLAYLIST; j++) {
    //             Content content = Content.builder()
    //                 .title("콘텐츠" + i + "-" + j)
    //                 .description("설명")
    //                 .type(Type.MOVIE)
    //                 .sourceId(System.nanoTime())  // 고유한 sourceId
    //                 .build();
    //             content = contentRepository.save(content);
    //
    //             // 콘텐츠에 태그 추가
    //             contentTagRepository.save(new ContentTag(content, tag1));
    //             contentTagRepository.save(new ContentTag(content, tag2));
    //
    //             // 플레이리스트-콘텐츠 연결
    //             playlistContentRepository.save(new PlaylistContent(playlist, content));
    //         }
    //     }
    //
    //     entityManager.flush();
    //     entityManager.clear();
    // }
    //
    // @Test
    // @DisplayName("[Playlist] 목록 조회 - limit 5")
    // void 플레이리스트_목록_조회() {
    //     // given
    //     int limit = 5;
    //     // 이상적: select + totalCount + owner + SubscribedCount + contents + tags = 6
    //     int expectedQueries = 6;
    //
    //     PlaylistSearchRequest request = new PlaylistSearchRequest();
    //     request.setLimit(limit);
    //     request.setSortBy(PlaylistSearchRequest.PlaylistSortBy.updatedAt);
    //     request.setSortDirection(PlaylistSearchRequest.SortDirection.DESCENDING);
    //
    //     // when
    //     long actual = queryCounter.count(() -> {
    //         playlistService.searchPlaylists(request, userId);
    //     });
    //
    //     // then
    //     printResult("GET /api/playlists", limit, expectedQueries, actual);
    //     assertThat(actual).isLessThanOrEqualTo(expectedQueries);
    // }
    //
    // @Test
    // @DisplayName("[Playlist] 단건 조회")
    // void 플레이리스트_단건_조회() {
    //     // given
    //     // 이상적: select + totalCount + owner + SubscribedCount + contents + tags = 6
    //     int expectedQueries = 6;
    //
    //     // when
    //     long actual = queryCounter.count(() -> {
    //         playlistService.getPlaylist(playlistId, userId);
    //     });
    //
    //     // then
    //     printResult("GET /api/playlists/{id}", 1, expectedQueries, actual);
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
