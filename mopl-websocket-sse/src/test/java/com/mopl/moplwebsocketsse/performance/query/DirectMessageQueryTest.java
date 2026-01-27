package com.mopl.moplwebsocketsse.performance.query;

class DirectMessageQueryTest {

}


//
// import static org.assertj.core.api.Assertions.*;
//
// import java.util.ArrayList;
// import java.util.List;
// import java.util.UUID;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.transaction.annotation.Transactional;
//
// import com.mopl.moplwebsocketsse.domain.common.enums.SortDirection;
// import com.mopl.moplwebsocketsse.domain.directMessage.dto.ConversationSearchRequest;
// import com.mopl.moplwebsocketsse.domain.directMessage.dto.CursorResponseConversationDto;
// import com.mopl.moplwebsocketsse.domain.directMessage.dto.CursorResponseDirectMessageDto;
// import com.mopl.moplwebsocketsse.domain.directMessage.dto.DirectMessageSearchRequest;
// import com.mopl.moplwebsocketsse.domain.directMessage.entity.Conversation;
// import com.mopl.moplwebsocketsse.domain.directMessage.entity.ConversationParticipants;
// import com.mopl.moplwebsocketsse.domain.directMessage.entity.DirectMessage;
// import com.mopl.moplwebsocketsse.domain.directMessage.repository.ConversationParticipantsRepository;
// import com.mopl.moplwebsocketsse.domain.directMessage.repository.ConversationRepository;
// import com.mopl.moplwebsocketsse.domain.directMessage.repository.DirectMessageRepository;
// import com.mopl.moplwebsocketsse.domain.directMessage.service.ConversationService;
// import com.mopl.moplwebsocketsse.domain.directMessage.service.DirectMessageService;
// import com.mopl.moplwebsocketsse.domain.user.entity.User;
// import com.mopl.moplwebsocketsse.domain.user.repository.UserRepository;
// import com.mopl.moplwebsocketsse.util.QueryCounter;
//
// import jakarta.persistence.EntityManager;
//
// @SpringBootTest
// @ActiveProfiles("test")
// @Transactional
// class DirectMessageQueryTest {
//
//     @Autowired
//     private ConversationService conversationService;
//
//     @Autowired
//     private DirectMessageService directMessageService;
//
//     @Autowired
//     private ConversationRepository conversationRepository;
//
//     @Autowired
//     private ConversationParticipantsRepository participantsRepository;
//
//     @Autowired
//     private DirectMessageRepository directMessageRepository;
//
//     @Autowired
//     private UserRepository userRepository;
//
//     @Autowired
//     private QueryCounter queryCounter;
//
//     @Autowired
//     private EntityManager em;
//
//     private User requester;
//     private List<User> otherUsers;
//     private List<Conversation> conversations;
//     private static final int CONVERSATION_COUNT = 5;
//     private static final int MESSAGE_COUNT = 10;
//
//     @BeforeEach
//     void setUp() {
//         String uniqueId = UUID.randomUUID().toString().substring(0, 8);
//
//         // 1. 요청자(나) 생성
//         requester = new User(
//             "요청자_" + uniqueId,
//             "requester_" + uniqueId + "@test.com",
//             "password"
//         );
//         userRepository.save(requester);
//
//         // 2. 상대방 유저들 생성
//         otherUsers = new ArrayList<>();
//         for (int i = 0; i < CONVERSATION_COUNT; i++) {
//             User user = new User(
//                 "상대방" + i + "_" + uniqueId,
//                 "other_" + uniqueId + "_" + i + "@test.com",
//                 "password"
//             );
//             userRepository.save(user);
//             otherUsers.add(user);
//         }
//
//         // 3. 대화 생성 (요청자 <-> 상대방)
//         conversations = new ArrayList<>();
//         for (int i = 0; i < CONVERSATION_COUNT; i++) {
//             Conversation conversation = new Conversation();
//             conversationRepository.save(conversation);
//
//             participantsRepository.save(new ConversationParticipants(conversation, requester));
//             participantsRepository.save(new ConversationParticipants(conversation, otherUsers.get(i)));
//
//             // 4. 각 대화에 메시지 추가
//             for (int j = 0; j < MESSAGE_COUNT; j++) {
//                 User sender = (j % 2 == 0) ? requester : otherUsers.get(i);
//                 DirectMessage message = new DirectMessage(conversation, sender, "메시지 " + j);
//                 directMessageRepository.save(message);
//
//                 if (j == MESSAGE_COUNT - 1) {
//                     conversation.updateLastMessage(message);
//                 }
//             }
//
//             conversations.add(conversation);
//         }
//
//         em.flush();
//         em.clear();
//     }
//
//     @Test
//     @DisplayName("GET /api/conversations - 대화 목록 조회 N+1 테스트")
//     void findConversations_N1QueryTest() {
//         // given
//         int limit = 5;
//         ConversationSearchRequest request = new ConversationSearchRequest(
//             null,
//             null,
//             null,
//             limit,
//             SortDirection.DESCENDING
//         );
//
//         // 예상 쿼리:
//         // 1. countByParticipantId (totalCount)
//         // 2. findByParticipantIdWithCursor (conversations)
//         // 3. findByConversationIdInWithUser (participants 배치 + fetch join)
//         // 4. findConversationIdsWithUnreadMessages (unread 배치)
//         // 5. findByIdInWithLastMessage (lastMessage 배치 + fetch join)
//         int expectedQueries = 5;
//
//         // when
//         long queryCount = queryCounter.count(() -> {
//             CursorResponseConversationDto result =
//                 conversationService.findConversations(requester.getId(), request);
//
//             assertThat(result.data()).hasSize(CONVERSATION_COUNT);
//         });
//
//         // then
//         System.out.println("==============================");
//         System.out.println("GET /api/conversations");
//         System.out.println("==============================");
//         System.out.println("대화 수  : " + CONVERSATION_COUNT);
//         System.out.println("limit   : " + limit);
//         System.out.println("예상 쿼리 : " + expectedQueries);
//         System.out.println("실제 쿼리 : " + queryCount);
//
//         if (queryCount > expectedQueries) {
//             System.out.println("결과     : N+1 의심 (+" + (queryCount - expectedQueries) + " 추가 쿼리)");
//         } else {
//             System.out.println("결과     : 정상");
//         }
//         System.out.println("==============================");
//
//         assertThat(queryCount).isLessThanOrEqualTo(expectedQueries);
//     }
//
//     @Test
//     @DisplayName("GET /api/conversations/{conversationId}/direct-messages - DM 목록 조회 N+1 테스트")
//     void findMessages_N1QueryTest() {
//         // given
//         Conversation targetConversation = conversations.get(0);
//         int limit = 10;
//         DirectMessageSearchRequest request = new DirectMessageSearchRequest(
//             null,
//             null,
//             limit,
//             SortDirection.DESCENDING
//         );
//
//         // 예상 쿼리:
//         // 1. existsByConversationIdAndUserId (참여자 확인)
//         // 2. findByConversationIdWithUser (participants + fetch join)
//         // 3. countByConversationId (totalCount)
//         // 4. findByConversationIdWithCursor (messages + sender fetch join)
//         int expectedQueries = 4;
//
//         // when
//         long queryCount = queryCounter.count(() -> {
//             CursorResponseDirectMessageDto result =
//                 directMessageService.findMessages(targetConversation.getId(), requester.getId(), request);
//
//             assertThat(result.data()).hasSize(MESSAGE_COUNT);
//         });
//
//         // then
//         System.out.println("==============================");
//         System.out.println("GET /api/conversations/{conversationId}/direct-messages");
//         System.out.println("==============================");
//         System.out.println("메시지 수 : " + MESSAGE_COUNT);
//         System.out.println("limit   : " + limit);
//         System.out.println("예상 쿼리 : " + expectedQueries);
//         System.out.println("실제 쿼리 : " + queryCount);
//
//         if (queryCount > expectedQueries) {
//             System.out.println("결과     : N+1 의심 (+" + (queryCount - expectedQueries) + " 추가 쿼리)");
//         } else {
//             System.out.println("결과     : 정상");
//         }
//         System.out.println("==============================");
//
//         assertThat(queryCount).isLessThanOrEqualTo(expectedQueries);
//     }
// }
