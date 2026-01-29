# MOPL - Content & Community Streaming Platform

MOPL은 콘텐츠 탐색, 소셜 기능, 실시간 시청 세션을 제공하는 커뮤니티 기반 스트리밍 플랫폼의 백엔드 서버입니다.

## 목차

- [프로젝트 구조](#프로젝트-구조)
- [기술 스택](#기술-스택)
- [주요 기능](#주요-기능)
- [API 명세](#api-명세)
- [인증 및 인가](#인증-및-인가)
- [데이터베이스 스키마](#데이터베이스-스키마)
- [검색 엔진 (Elasticsearch)](#검색-엔진-elasticsearch)
- [실시간 통신](#실시간-통신)
- [이벤트 스트리밍 (Kafka)](#이벤트-스트리밍-kafka)
- [캐싱 (Redis)](#캐싱-redis)
- [파일 스토리지 (S3)](#파일-스토리지-s3)
- [에러 처리](#에러-처리)
- [아키텍처](#아키텍처)
- [Nginx 라우팅](#nginx-라우팅)
- [개발 환경 설정](#개발-환경-설정)
- [Docker 구성](#docker-구성)
- [배포](#배포)
- [모니터링](#모니터링)

### 시스템 구성도

<img width="1154" height="560" alt="image" src="https://github.com/user-attachments/assets/18009b49-b621-4a2a-be72-56bcdbdd93d2" />
<img width="1175" height="525" alt="image" src="https://github.com/user-attachments/assets/0793362f-af98-4be6-9af0-e3c9d480a047" />

### ERD 개요

<img width="1147" height="501" alt="image" src="https://github.com/user-attachments/assets/6b34995f-165f-401e-9112-2f0b34c22927" />

---

## 프로젝트 구조

멀티 모듈 Gradle 프로젝트로 구성되어 있습니다.

```
mopl_spring/
├── mopl-core/                  # 메인 REST API 서버
│   └── src/main/java/com/mopl/core/
│       ├── domain/
│       │   ├── auth/           # 인증 (JWT, OAuth2)
│       │   ├── content/        # 콘텐츠 관리
│       │   ├── follow/         # 팔로우/팔로워
│       │   ├── playlist/       # 플레이리스트
│       │   ├── review/         # 리뷰 및 평점
│       │   └── user/           # 사용자 관리
│       ├── config/             # Spring 설정
│       ├── security/           # 보안 설정 (JWT, OAuth2)
│       └── common/             # 공통 유틸리티
│
├── mopl-websocket-sse/         # 실시간 통신 서버 (WebSocket/SSE)
│   └── src/main/java/com/mopl/websocketsse/
│       ├── domain/
│       │   ├── conversation/   # 대화방 관리
│       │   ├── directmessage/  # 1:1 메시지
│       │   ├── notification/   # 알림
│       │   ├── sse/            # Server-Sent Events
│       │   └── watchingsession/# 시청 세션
│       └── config/             # WebSocket/Kafka 설정
│
├── deploy/                     # 배포 스크립트 및 설정
│   ├── docker-compose.yml      # 개발 환경 컨테이너
│   ├── docker-compose.dev.yml  # 개발 전용 구성
│   ├── nginx/                  # Nginx 리버스 프록시 설정
│   └── scripts/                # 배포 자동화 스크립트
│
└── .github/workflows/          # CI/CD 파이프라인
    ├── deploy-spring.yml       # mopl-core 배포
    ├── deploy-websocket.yml    # mopl-websocket-sse 배포
    └── deploy-nginx.yml        # Nginx 배포
```

## 기술 스택

| 분류 | 기술 | 버전/세부사항 |
|------|------|---------------|
| **Language** | Java | 21 (Jakarta EE) |
| **Framework** | Spring Boot | 3.5.9 |
| **Security** | Spring Security | 6 (Stateless, JWT) |
| **Database** | PostgreSQL | 17-alpine |
| **Cache** | Redis | 7.4-alpine (Lettuce) |
| **Search** | Elasticsearch | Nori 한국어 형태소 분석기 |
| **Messaging** | Apache Kafka | 4.1.1 (KRaft 모드) |
| **ORM** | JPA / Hibernate | QueryDSL 타입 안전 쿼리 |
| **Auth** | JWT (Nimbus JOSE) | Access + Refresh 토큰 |
| **OAuth2** | Spring OAuth2 Client | Google (OIDC), Kakao |
| **Object Mapping** | MapStruct | Entity-DTO 자동 매핑 |
| **Cloud** | AWS | S3, SES, ECR, CodeDeploy |
| **Monitoring** | Micrometer + Prometheus | Spring Actuator |
| **DB Migration** | Flyway | Production 환경 전용 |
| **CI/CD** | GitHub Actions | Docker + AWS CodeDeploy |
| **Reverse Proxy** | Nginx | WebSocket/SSE 라우팅 |
| **Etc.** | Lombok, Thymeleaf | 보일러플레이트 제거, 이메일 템플릿 |

## 주요 기능

### mopl-core (REST API)

| 기능 | 설명 |
|------|------|
| **콘텐츠 관리** | 영화/시리즈 CRUD, Elasticsearch 기반 전문 검색, 태그 분류, 멀티파트 이미지 업로드 |
| **사용자 관리** | 회원가입, 프로필 관리 (이미지 포함), 역할 기반 접근 제어 (ADMIN/USER), 계정 잠금 |
| **인증** | JWT 토큰 (Access/Refresh), Google/Kakao OAuth2 소셜 로그인, 비밀번호 재설정 |
| **팔로우** | 사용자 간 팔로우/언팔로우, 팔로워 수 조회, 팔로우 여부 확인 |
| **플레이리스트** | 콘텐츠 컬렉션 생성/수정/삭제, 콘텐츠 추가/제거, 구독/구독 해제 |
| **리뷰** | 콘텐츠 리뷰 작성 (1.0~5.0 평점), 수정/삭제, 콘텐츠별 평균 평점 자동 계산 |

### mopl-websocket-sse (실시간 통신)

| 기능 | 설명 |
|------|------|
| **SSE 알림** | Server-Sent Events 기반 실시간 푸시 알림, `Last-Event-ID` 헤더로 누락 이벤트 복구 |
| **시청 세션** | 현재 콘텐츠를 시청 중인 사용자 실시간 추적, Heartbeat 기반 세션 유지 |
| **1:1 메시지** | STOMP + WebSocket 기반 실시간 채팅, 읽음 처리 |
| **대화방** | 사용자 간 대화방 생성/관리, 마지막 읽은 시점 추적, 분산 락 기반 동시성 제어 |
| **알림 이벤트** | Kafka Consumer를 통한 비동기 알림 수신 및 SSE 전달 |

---

## API 명세

### Content API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| `GET` | `/api/contents` | 콘텐츠 검색 (커서 페이지네이션) | 전체 |
| `GET` | `/api/contents/{id}` | 콘텐츠 상세 조회 | 전체 |
| `POST` | `/api/contents` | 콘텐츠 생성 (JSON 또는 Multipart) | ADMIN |
| `PATCH` | `/api/contents/{id}` | 콘텐츠 수정 | ADMIN |
| `DELETE` | `/api/contents/{id}` | 콘텐츠 삭제 | ADMIN |

**콘텐츠 검색 파라미터:**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `keyword` | String | 제목/설명 전문 검색 (Nori 형태소 분석) |
| `type` | Enum | 콘텐츠 유형 필터 (MOVIE, SERIES 등) |
| `tags` | List | 태그 필터 (다중 선택) |
| `sortBy` | Enum | 정렬 기준: `createdAt`, `rate`, `watcherCount` |
| `sortDirection` | Enum | `ASCENDING` / `DESCENDING` |
| `cursor` | String | 커서 페이지네이션 토큰 (Base64 인코딩) |
| `limit` | Integer | 페이지 크기 |

**커서 페이지네이션 응답:**

```json
{
  "data": [{ "id": "...", "title": "...", ... }],
  "nextCursor": "base64_encoded_cursor",
  "nextIdAfter": "uuid",
  "hasNext": true,
  "totalCount": 1234,
  "sortBy": "createdAt",
  "sortDirection": "DESCENDING"
}
```

커서는 `ID|CreatedAt|AverageRating|WatcherCount`를 Base64 인코딩한 값으로, 다음 페이지 요청 시 전달합니다. `limit+1`개를 조회하여 `hasNext`를 판단합니다.

### User API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| `POST` | `/api/users` | 회원가입 | 전체 |
| `GET` | `/api/users/{id}` | 사용자 프로필 조회 | 인증 |
| `GET` | `/api/users` | 전체 사용자 조회 | ADMIN |
| `PATCH` | `/api/users/{id}` | 프로필 수정 (Multipart 이미지 포함) | 본인 |
| `PATCH` | `/api/users/{id}/password` | 비밀번호 변경 | 본인 |
| `PATCH` | `/api/users/{id}/role` | 역할 변경 | ADMIN |
| `PATCH` | `/api/users/{id}/locked` | 계정 잠금/해제 | ADMIN |

### Auth API

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/api/auth/sign-in` | 로컬 로그인 (이메일 + 비밀번호) |
| `POST` | `/api/auth/sign-out` | 로그아웃 (토큰 무효화) |
| `POST` | `/api/auth/refresh` | Refresh 토큰으로 Access 토큰 재발급 |
| `POST` | `/api/auth/reset-password` | 비밀번호 재설정 이메일 발송 |
| `GET` | `/api/auth/csrf-token` | CSRF 토큰 발급 |
| `GET` | `/login/oauth2/code/{provider}` | OAuth2 콜백 (google, kakao) |

### Follow API

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/api/follows` | 팔로우 (자기 자신 팔로우 불가) |
| `DELETE` | `/api/follows/{id}` | 언팔로우 |
| `GET` | `/api/follows/followed-by-me?followeeId={id}` | 특정 사용자 팔로우 여부 확인 |
| `GET` | `/api/follows/count?followeeId={id}` | 팔로워 수 조회 |

### Playlist API

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/api/playlists` | 플레이리스트 목록 |
| `POST` | `/api/playlists` | 플레이리스트 생성 |
| `GET` | `/api/playlists/{id}` | 플레이리스트 상세 조회 |
| `PATCH` | `/api/playlists/{id}` | 플레이리스트 수정 |
| `DELETE` | `/api/playlists/{id}` | 플레이리스트 삭제 |
| `POST` | `/api/playlists/{id}/contents/{contentId}` | 콘텐츠 추가 |
| `DELETE` | `/api/playlists/{id}/contents/{contentId}` | 콘텐츠 제거 |
| `POST` | `/api/playlists/{id}/subscription` | 구독 |
| `DELETE` | `/api/playlists/{id}/subscription` | 구독 해제 |

### Review API

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/api/reviews` | 리뷰 작성 (1.0~5.0 평점, 콘텐츠당 1개) |
| `GET` | `/api/reviews` | 리뷰 검색 |
| `PATCH` | `/api/reviews/{id}` | 리뷰 수정 |
| `DELETE` | `/api/reviews/{id}` | 리뷰 삭제 |

### 실시간 API (mopl-websocket-sse)

| 프로토콜 | Endpoint | 설명 |
|----------|----------|------|
| SSE | `GET /api/sse` | 실시간 알림 스트림 구독 |
| HTTP | `GET /api/notifications` | 알림 목록 조회 |
| HTTP | `GET /api/contents/{id}/watching-sessions` | 콘텐츠 시청 중인 사용자 조회 |
| HTTP | `GET /api/users/{id}/watching-sessions` | 사용자 시청 세션 조회 |
| SSE | `GET /api/watching-sessions/sse` | 시청자 수 실시간 스트림 |
| HTTP | `POST /api/conversations` | 대화방 생성 |
| HTTP | `GET /api/conversations` | 대화방 목록 조회 |
| HTTP | `GET /api/conversations/{id}` | 대화방 상세 조회 |
| HTTP | `GET /api/conversations/with?userId={id}` | 특정 사용자와의 대화방 조회 |
| WebSocket | `/pub/conversations/{id}/direct-messages` | 실시간 메시지 전송 (STOMP) |

---

## 인증 및 인가

### JWT 인증 흐름

```
1. 로그인 요청 (POST /api/auth/sign-in)
   ↓
2. 자격 증명 검증 (이메일 + 비밀번호)
   ↓
3. Access Token 발급 (응답 본문) + Refresh Token 발급 (HTTP-Only 쿠키)
   ↓
4. 이후 요청마다 Authorization: Bearer {accessToken} 헤더 포함
   ↓
5. JwtAuthenticationFilter에서 토큰 검증
   ↓
6. Access Token 만료 시 → POST /api/auth/refresh 로 재발급
```

| 항목 | Access Token | Refresh Token |
|------|-------------|---------------|
| **저장 위치** | 클라이언트 메모리 | HTTP-Only 쿠키 |
| **만료 시간** | 짧은 기간 (설정 가능) | 긴 기간 (설정 가능) |
| **용도** | API 인증 | Access Token 재발급 |
| **무효화** | Redis에서 관리 | 로그아웃 시 삭제 |

- 사용자당 단일 활성 토큰 정책 (`jwt.max-active: 1`) - 새 로그인 시 기존 세션 무효화
- Stateless 세션 관리 (SESSIONLESS 정책)
- CSRF 보호: 쿠키 기반 CSRF 토큰

### OAuth2 소셜 로그인

| 제공자 | 프로토콜 | Scope | 수집 정보 |
|--------|----------|-------|-----------|
| **Google** | OpenID Connect | openid, profile, email | 이름, 이메일, 프로필 이미지 |
| **Kakao** | Authorization Code | profile_nickname, profile_image | 닉네임, 프로필 이미지 |

- 소셜 로그인 사용자는 비밀번호 없이 가입 (password = NULL)
- 쿠키 기반 인가 요청 저장 (`HttpCookieOAuth2AuthorizationRequestRepository`)
- 성공/실패 핸들러를 통한 리다이렉트 처리

### 권한 체계

| 역할 | 권한 |
|------|------|
| `ADMIN` | 콘텐츠 CRUD, 사용자 역할 변경, 계정 잠금/해제, 전체 사용자 조회 |
| `USER` | 프로필 관리, 리뷰 작성, 팔로우, 플레이리스트, 메시지, 시청 세션 |

- `@PreAuthorize` 어노테이션으로 메서드 수준 보안 적용
- 잠긴 계정 (`locked=true`)은 로그인 거부

---

## 데이터베이스 스키마

### 주요 테이블

#### users
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | UUID (PK) | 사용자 고유 ID |
| `name` | varchar(255) | 사용자명 |
| `email` | varchar(255), UNIQUE | 이메일 (로그인 ID) |
| `password` | varchar(255), nullable | 비밀번호 해시 (OAuth 사용자는 NULL) |
| `profile_image_url` | varchar(255) | S3 프로필 이미지 URL |
| `role` | enum | USER, ADMIN |
| `provider` | enum | LOCAL, GOOGLE, KAKAO |
| `locked` | boolean | 계정 잠금 여부 |
| `created_at` | timestamptz | 생성일 |

#### contents
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | UUID (PK) | 콘텐츠 고유 ID |
| `title` | varchar(255) | 제목 |
| `description` | TEXT | 설명 |
| `thumbnail_url` | varchar(255) | 썸네일 이미지 URL |
| `type` | enum | MOVIE, SERIES 등 |
| `source_id` | int8 | 외부 소스 식별자 |
| `average_rating` | float8 | 평균 평점 (리뷰 기반 자동 계산) |
| `review_count` | int4 | 리뷰 수 |
| `created_at` | timestamptz | 생성일 |

- **Unique Constraint:** `(source_id, type)` 조합 유일

#### reviews
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | UUID (PK) | 리뷰 고유 ID |
| `author_id` | UUID (FK → users) | 작성자 |
| `content_id` | UUID (FK → contents) | 대상 콘텐츠 |
| `text` | TEXT | 리뷰 내용 |
| `rating` | float8 | 평점 (1.0 ~ 5.0) |
| `created_at` | timestamptz | 작성일 |
| `updated_at` | timestamptz | 수정일 |

#### follows
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | UUID (PK) | 팔로우 고유 ID |
| `follower_id` | UUID (FK → users) | 팔로우 하는 사용자 |
| `followee_id` | UUID (FK → users) | 팔로우 받는 사용자 |
| `created_at` | timestamptz | 팔로우 일시 |

#### playlists / playlist_contents / playlist_subscriptions
| 테이블 | 주요 컬럼 | 설명 |
|--------|-----------|------|
| `playlists` | owner_id, title, description | 사용자가 생성한 콘텐츠 컬렉션 |
| `playlist_contents` | playlist_id, content_id | 플레이리스트 내 콘텐츠 |
| `playlist_subscriptions` | playlist_id, user_id | 플레이리스트 구독 |

#### conversations / conversation_participants / direct_messages
| 테이블 | 주요 컬럼 | 설명 |
|--------|-----------|------|
| `conversations` | last_message_id | 대화방 (마지막 메시지 참조) |
| `conversation_participants` | conversation_id, user_id, last_read_at | 참여자 및 읽은 위치 |
| `direct_messages` | conversation_id, sender_id, content, read_at | 메시지 내용 및 읽음 상태 |

- `conversation_participants`에 `(conversation_id, user_id)` Unique 제약 조건
- `direct_messages`는 대화방 삭제 시 CASCADE 삭제

#### notifications
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | UUID (PK) | 알림 고유 ID |
| `receiver_id` | UUID (FK → users) | 수신자 |
| `event_id` | UUID, UNIQUE | 이벤트 식별자 (중복 방지) |
| `title` | varchar | 알림 제목 |
| `content` | TEXT | 알림 내용 |
| `level` | varchar | 알림 레벨 |
| `created_at` | timestamp | 생성일 |
| `read_at` | timestamp | 읽은 시점 |

### 데이터 초기화

개발 환경(`dev` 프로필)에서는 애플리케이션 시작 시 자동으로 데이터를 초기화합니다:

- **스키마**: `classpath:sql/schema.sql` 실행
- **더미 데이터**: `classpath:sql/dummy.sql` 실행 (100명 사용자, 콘텐츠, 태그, 리뷰 등)
- **관리자 계정**: `AdminInitializer`가 기본 관리자/테스트 계정 생성
- **검색 인덱스**: `ContentSyncRunner`가 콘텐츠를 Elasticsearch에 동기화

Production 환경에서는 **Flyway**를 통해 마이그레이션을 관리합니다.

---

## 검색 엔진 (Elasticsearch)

### 인덱스 구조

인덱스명: `contents`

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | keyword | 문서 ID |
| `contentId` | keyword | 콘텐츠 UUID |
| `type` | keyword | 콘텐츠 유형 |
| `title` | text (nori) | 제목 (한국어 형태소 분석) |
| `description` | text (nori) | 설명 (한국어 형태소 분석) |
| `thumbnailUrl` | keyword | 썸네일 URL |
| `tags` | keyword[] | 태그 목록 |
| `averageRating` | double | 평균 평점 |
| `reviewCount` | integer | 리뷰 수 |
| `watcherCount` | long | 현재 시청자 수 |
| `createdAt` | date | 생성일 |

### Nori 분석기 설정

한국어 전문 검색을 위해 Nori 형태소 분석기를 사용합니다:

```json
{
  "analyzer": {
    "nori": {
      "type": "custom",
      "tokenizer": "nori_tokenizer",
      "filter": ["lowercase", "nori_part_of_speech"]
    }
  },
  "tokenizer": {
    "nori_tokenizer": {
      "type": "nori_tokenizer",
      "decompound_mode": "mixed"
    }
  }
}
```

- **decompound_mode: mixed** - 복합어를 원형과 분해된 형태 모두 인덱싱
- **nori_part_of_speech** - 조사, 접속사 등 불필요한 품사 필터링 (E, IC, J, MAG 등 제거)

### 검색 동작

1. `keyword`가 주어지면 `title`과 `description` 필드에 대해 Nori 분석기 기반 매칭
2. `type`, `tags` 필터 적용
3. 정렬 기준(`createdAt`, `rate`, `watcherCount`)에 따라 커서 페이지네이션 적용
4. `watcherCount`는 Redis에서 실시간 동기화

### 클라이언트 설정

- Max Connections: 100
- Socket Timeout: 60초
- Connection Timeout: 10초

---

## 실시간 통신

### WebSocket (STOMP)

```
클라이언트 ──── /ws (SockJS) ──── STOMP 프로토콜
                                     │
                          ┌──────────┼──────────┐
                          │          │          │
                    Inbound Prefix  Broker  Subscription
                       /pub       Simple      /sub
```

| 설정 | 값 |
|------|-----|
| **엔드포인트** | `/ws` (SockJS 폴백 지원) |
| **메시지 발행 prefix** | `/pub` |
| **구독 prefix** | `/sub` |
| **Heartbeat** | 10초 간격 (양방향) |
| **프로토콜** | STOMP over WebSocket |

**인터셉터:**
- `JwtChannelInterceptor` - WebSocket 연결 시 JWT 토큰 검증
- `WatchingSessionHeartbeatInterceptor` - 시청 세션 Heartbeat 유지

**메시지 전송 예시:**

```
발행: /pub/conversations/{conversationId}/direct-messages
구독: /sub/conversations/{conversationId}/direct-messages
```

### SSE (Server-Sent Events)

- 알림 구독: `GET /api/sse` - EventSource 연결
- `Last-Event-ID` 헤더를 통한 누락 이벤트 재전송 지원
- 연결 유지 시간: Nginx에서 3600초 (1시간) 타임아웃 설정

---

## 이벤트 스트리밍 (Kafka)

### 토픽 구성

| 토픽 | 발행자 | 소비자 | 용도 |
|------|--------|--------|------|
| `notification.events` | mopl-core | mopl-websocket-sse | 실시간 알림 (SSE 전달) |
| `watching.session.started` | mopl-websocket-sse | mopl-core | 시청 시작 이벤트 → 팔로워 알림 |

### 이벤트 흐름 예시

```
[사용자 A가 콘텐츠 시청 시작]
  → mopl-websocket-sse: WatchingSession 생성
  → Kafka 발행: watching.session.started (actorName, contentTitle, watchingSessionId)
  → mopl-core 소비: WatchingSessionStartedConsumer
  → 팔로워 조회 (FolloweeActivityNotifier)
  → Kafka 발행: notification.events (각 팔로워에게)
  → mopl-websocket-sse 소비: KafkaConsumer → NotificationEventHandler
  → SSE로 팔로워 클라이언트에 실시간 전달
```

### Kafka 설정

| 항목 | Producer | Consumer |
|------|----------|----------|
| **직렬화** | StringSerializer | StringDeserializer |
| **acks** | all (모든 복제본 확인) | - |
| **offset reset** | - | earliest |
| **커밋 방식** | - | manual (수동 확인) |
| **에러 처리** | - | 3회 재시도 (1초 간격) → DLQ (`{topic}.dlq`) |

---

## 캐싱 (Redis)

### 용도

| 용도 | 설명 |
|------|------|
| **JWT 토큰 관리** | 활성 토큰 저장, 로그아웃 시 무효화, 단일 세션 관리 |
| **시청자 수 캐싱** | 콘텐츠별 현재 시청자 수 임시 캐시 (Elasticsearch 동기화) |
| **세션 데이터** | 사용자 세션 관련 데이터 저장 |

### 설정

```yaml
redis:
  host: redis
  port: 6379
  lettuce:
    pool:
      max-active: 100    # 최대 활성 연결
      max-idle: 20       # 최대 유휴 연결
      min-idle: 5        # 최소 유휴 연결
```

- **Key 직렬화**: `StringRedisSerializer`
- **Value 직렬화**: `GenericJackson2JsonRedisSerializer` (JSON)

---

## 파일 스토리지 (S3)

### 구성

| 항목 | 값 |
|------|-----|
| **리전** | ap-northeast-2 (서울) |
| **버킷** | 환경 변수로 설정 (`AWS_S3_BUCKET_NAME`) |
| **파일 크기 제한** | 최대 10MB |
| **인증** | AWS_ACCESS_KEY / AWS_SECRET_KEY |

### 사용 사례

- **프로필 이미지**: 사용자 아바타 업로드 (`PATCH /api/users/{id}` Multipart)
- **콘텐츠 썸네일**: 콘텐츠 커버 이미지 업로드 (`POST /api/contents` Multipart)

---

## 에러 처리

### 전역 예외 처리

`GlobalExceptionHandler`에서 모든 예외를 일관된 형식으로 처리합니다.

### 응답 형식

```json
{
  "timestamp": "2024-01-29T12:00:00Z",
  "code": "CONTENT_NOT_FOUND",
  "message": "콘텐츠를 찾을 수 없습니다.",
  "details": { "contentId": "uuid" },
  "exceptionType": "ContentNotFoundException",
  "status": 404
}
```

### 예외 계층 구조

```
BaseException
├── Content
│   └── ContentNotFoundException (404)
├── User
│   ├── UserNotFoundException (404)
│   ├── DuplicateEmailException (409)
│   ├── ForbiddenUserAccessException (403)
│   └── AuthPasswordException (400)
├── Review
│   ├── ReviewNotFoundException (404)
│   ├── InvalidRatingException (400)
│   ├── InvalidReviewTextException (400)
│   ├── ReviewAlreadyExistsException (409)
│   └── ForbiddenReviewAccessException (403)
├── Follow
│   ├── FollowNotFoundException (404)
│   ├── FollowAlreadyExistsException (409)
│   ├── CannotFollowYourselfException (400)
│   └── ForbiddenFollowAccessException (403)
├── Auth
│   ├── InValidCredentialException (401)
│   ├── LockedUserAccessException (401)
│   ├── InValidAccessTokenException (401)
│   ├── InValidRefreshTokenException (401)
│   └── TokenGenerateException (500)
└── Conversation
    ├── ConversationNotFoundException (404)
    ├── ConversationAlreadyExistsException (409)
    ├── SelfConversationNotAllowedException (400)
    ├── NotConversationParticipantException (403)
    └── ConversationLockAcquisitionFailedException (500)
```

50개 이상의 에러 코드가 `ErrorCode` Enum으로 정의되어 HTTP 상태 코드에 매핑됩니다.

---

## 아키텍처

### 코드 아키텍처

```
Controller (요청 수신, 응답 반환)
    │
    ├── Request DTO 검증
    │
    ▼
Service (비즈니스 로직)
    │
    ├── MapStruct (Entity ↔ DTO 변환)
    ├── Kafka Producer (이벤트 발행)
    │
    ▼
Repository (데이터 접근)
    │
    ├── JPA Repository (기본 CRUD)
    ├── QueryDSL (복잡한 동적 쿼리)
    └── Elasticsearch Repository (검색)
```

### 주요 설계 패턴

| 패턴 | 적용 |
|------|------|
| **레이어드 아키텍처** | Controller → Service → Repository 계층 분리 |
| **도메인 주도 패키지** | 도메인별 패키지 구성 (auth, content, user 등) |
| **DTO 패턴** | Request/Response DTO로 계층 간 데이터 전달 |
| **Builder 패턴** | Entity 생성 시 Builder 사용 |
| **Repository 패턴** | JPA + QueryDSL Custom Repository |
| **이벤트 주도** | Kafka를 통한 모듈 간 비동기 통신 |
| **분산 락** | Redis 기반 대화방 동시성 제어 |

---

## Nginx 라우팅

Nginx가 리버스 프록시로 동작하여 요청을 적절한 서비스로 라우팅합니다.

### 라우팅 규칙

| 경로 | 대상 서비스 | 프로토콜 | 비고 |
|------|------------|----------|------|
| `/ws` | websocket-server | WebSocket | Upgrade 헤더, 86400초 타임아웃 |
| `/api/sse*` | websocket-server | SSE | 버퍼링 비활성화, 3600초 타임아웃 |
| `/api/notifications` | websocket-server | HTTP/1.1 | 알림 API |
| `/api/conversations` | websocket-server | HTTP/1.1 | 대화방 API |
| `/api/direct-messages` | websocket-server | HTTP/1.1 | 메시지 API |
| `/api/watching-sessions/sse` | websocket-server | SSE | 시청자 수 실시간 스트림 |
| `/api/contents/*/watching-sessions` | websocket-server | HTTP/1.1 | 콘텐츠별 시청 세션 |
| `/api/users/*/watching-sessions` | websocket-server | HTTP/1.1 | 사용자별 시청 세션 |
| `/oauth2`, `/login` | spring-api | HTTP/1.1 | OAuth2 인증 |
| `/api/*` | spring-api | HTTP/1.1 | 일반 REST API |
| `/actuator/core/*` | spring-api | HTTP/1.1 | Core 헬스/메트릭 |
| `/actuator/ws/*` | websocket-server | HTTP/1.1 | WebSocket 헬스/메트릭 |

### 성능 설정

| 항목 | 값 |
|------|-----|
| Worker Processes | auto |
| Worker Connections | 4096 |
| Multi Accept | on |
| Keepalive Timeout | 60초 |
| Keepalive Requests | 10,000 |
| spring-api Keepalive | 64 connections |
| websocket-server Keepalive | 32 connections |

---

## 개발 환경 설정

### 사전 요구사항

- Java 21
- Docker & Docker Compose
- Gradle (Wrapper 포함)

### 1. 인프라 컨테이너 실행

```bash
cd deploy
docker compose -f docker-compose.dev.yml up -d
```

실행되는 서비스:
- PostgreSQL (5433) - DB: `app`, User: `app`
- Redis (6379) - AOF 영속화
- Elasticsearch (9200) - Nori 플러그인 포함
- Kafka (9092) - KRaft 모드 단일 브로커

### 2. 애플리케이션 실행

```bash
# mopl-core 실행
./gradlew :mopl-core:bootRun --args='--spring.profiles.active=dev'

# mopl-websocket-sse 실행
./gradlew :mopl-websocket-sse:bootRun --args='--spring.profiles.active=dev'
```

### 3. 초기 계정

| 계정 | 이메일 | 비밀번호 | 역할 |
|------|--------|----------|------|
| 관리자 | admin@mopl.com | 1234qwer | ADMIN |
| 테스트 | test@mopl.com | 1234qwer | USER |

### 프로필 구성

| 프로필 | 용도 | 특징 |
|--------|------|------|
| `dev` | 개발 환경 | 자동 스키마 초기화, 더미 데이터, SQL 로깅 |
| `prod` | 운영 환경 | Flyway 마이그레이션, 환경 변수 기반 설정, HTTPS |
| `local` | 로컬 개발 | 개발 환경 오버라이드 |
| `test` | 테스트 | H2 또는 테스트 DB |

### 환경 변수 (Production)

| 변수명 | 설명 |
|--------|------|
| `DATABASE_URL` | PostgreSQL 접속 URL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | DB 인증 정보 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 연결 정보 |
| `JWT_ACCESS_TOKEN_KEY` / `JWT_REFRESH_TOKEN_KEY` | JWT 서명 키 |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth2 |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` | Kakao OAuth2 |
| `AWS_ACCESS_KEY` / `AWS_SECRET_KEY` | AWS 인증 |
| `AWS_S3_BUCKET_NAME` | S3 버킷명 |
| `ELASTICSEARCH_HOST` | Elasticsearch 호스트 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 브로커 주소 |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | 이메일 SMTP |

---

## Docker 구성

### 멀티 스테이지 빌드

각 모듈은 동일한 2단계 Docker 빌드를 사용합니다:

```dockerfile
# Stage 1: 빌드
FROM gradle:8.5-jdk21 AS builder
# Gradle 의존성 해석 → JAR 컴파일

# Stage 2: 실행
FROM eclipse-temurin:21-jdk-jammy
# 경량 JDK 이미지에서 Spring Boot JAR 실행
```

### Docker Compose 서비스 (개발 환경)

| 서비스 | 이미지 | 포트 | 볼륨 |
|--------|--------|------|------|
| spring-api | mopl-core 빌드 | 8080 | - |
| websocket-server | mopl-websocket-sse 빌드 | 8080 (내부) | - |
| nginx | nginx | 80 | nginx.conf |
| postgres | postgres:17-alpine | 5433 | pg_data |
| redis | redis:7.4-alpine | 6379 | redis_data (AOF) |
| elasticsearch | custom (Nori 포함) | 9200, 9300 | es_data |
| kafka | kafka:4.1.1 | 9092, 29092 | kafka_data |

### 네트워크

모든 서비스는 `app-network` 브릿지 네트워크에서 통신합니다.

---

## 배포

### CI/CD 파이프라인

```
코드 Push (main 브랜치, 특정 모듈 변경 감지)
    │
    ▼
GitHub Actions
    ├── 1. Checkout & Java 21 Setup
    ├── 2. Gradle Build (clean build)
    ├── 3. AWS Credentials 설정
    ├── 4. ECR 로그인 & Docker 이미지 빌드/태그/푸시
    ├── 5. GitHub Secrets → .env 파일 생성
    ├── 6. 배포 패키지 생성 (appspec.yml + scripts + docker-compose)
    ├── 7. S3 업로드 (tar.gz)
    └── 8. CodeDeploy 트리거 → EC2 배포
```

### 워크플로우

| 파일 | 트리거 조건 | 대상 |
|------|-------------|------|
| `deploy-spring.yml` | main push + mopl-core/ 변경 | mopl-core 서비스 |
| `deploy-websocket.yml` | main push + mopl-websocket-sse/ 변경 | WebSocket/SSE 서비스 |
| `deploy-nginx.yml` | main push + nginx/ 변경 | Nginx 리버스 프록시 |

### 배포 아키텍처

```
GitHub Actions (CI)
    → AWS ECR (Docker 이미지 레지스트리)
    → S3 (배포 아티팩트 저장)
    → CodeDeploy (배포 오케스트레이션)
    → EC2 (실행 인스턴스)
```

---

## 모니터링

### Actuator 엔드포인트

| 엔드포인트 | 설명 |
|------------|------|
| `/actuator/health` | 헬스체크 (DB, 디스크 등) |
| `/actuator/info` | 애플리케이션 정보 |
| `/actuator/metrics` | 메트릭 조회 |
| `/actuator/env` | 환경 변수 조회 |
| `/actuator/prometheus` | Prometheus 스크래핑 엔드포인트 |

### 커스텀 메트릭

Micrometer를 통해 수집되는 주요 메트릭:

| 메트릭 | 설명 |
|--------|------|
| `content.search.elasticsearch` | Elasticsearch 검색 쿼리 소요 시간 |
| `content.search.count` | 검색 카운트 쿼리 소요 시간 |
| `content.search.watcherCount` | 시청자 수 조회 소요 시간 |
| `content.search.mapping` | 검색 결과 매핑 소요 시간 |
| HTTP 요청 히스토그램 | 요청별 응답 시간 분포 (percentiles) |

### DB 연결 풀

| 항목 | 값 |
|------|-----|
| **커넥션 풀** | HikariCP |
| **Max Pool Size** | 30 |
