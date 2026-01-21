-- =========================
-- 0) 기본 세팅
-- =========================
BEGIN;

-- (선택) 로컬 타임존 영향 줄이기
SET LOCAL TIME ZONE 'Asia/Seoul';

-- pgcrypto (gen_random_uuid() 등) 사용
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 랜덤 시드 고정(매번 완전 동일하진 않지만 분포가 안정됨)
SELECT setseed(0.4242);

-- =========================
-- 1) 여러 번 실행 가능하게 전체 데이터 삭제
--    FK가 얽혀있으니 한번에 TRUNCATE + CASCADE
-- =========================
TRUNCATE TABLE
    public.content_tags,
    public.playlist_contents,
    public.playlist_subscriptions,
    public.reviews,
    public.follows,
    public.playlists,
    public.direct_messages,
    public.conversation_participants,
    public.conversations,
    public.notifications,
    public.tags,
    public.contents,
    public.users
    CASCADE;

-- =========================
-- 2) USERS (100명, 전부 LOCAL)
-- =========================
WITH u AS (SELECT i,
                  gen_random_uuid()                                  AS id,
                  (now() - (random() * interval '365 days'))         AS created_at,
                  (i % 17 = 0)                                       AS locked,
                  ('user' || to_char(i, 'FM0000') || '@example.com') AS email,
                  CASE
                      WHEN i <= 10 THEN format('관리자%02s', i)
                      ELSE format('사용자%04s', i)
                      END                                            AS name,
                  'LOCAL'                                            AS provider,
                  CASE
                      WHEN i <= 10 THEN 'ADMIN'
                      ELSE 'USER'
                      END                                            AS role,
                  ('pw_' || to_char(i, 'FM0000'))                    As "password", -- 전원 패스워드
                  CASE
                      WHEN random() < 0.75 THEN format('https://picsum.photos/seed/u%04s/200/200', i)
                      ELSE NULL
                      END                                            AS profile_image_url
           FROM generate_series(1, 100) AS s(i))
INSERT
INTO public.users (locked, created_at, id, email, name, "password", profile_image_url, provider, "role")
SELECT locked,
       created_at,
       id,
       email,
       name,
       "password",
       profile_image_url,
       provider,
       role
FROM u;
-- =========================
-- 3) CONTENTS (300개, type 고르게, source_id 중복 없이)
-- =========================
WITH c AS (SELECT i,
                  gen_random_uuid()                          AS id,
                  (now() - (random() * interval '900 days')) AS created_at,
                  i::bigint                                  AS source_id,
                  CASE (i % 3)
                      WHEN 0 THEN 'MOVIE'
                      WHEN 1 THEN 'TV_SERIES'
                      ELSE 'SPORTS'
                      END                                    AS "type",
                  round((random() * 5)::numeric, 2)::float8  AS average_rating,
                  (random() * 5000)::int                     AS review_count,
                  format('%s 타이틀 %03s', CASE (i % 3) WHEN 0 THEN '영화' WHEN 1 THEN 'TV' ELSE '스포츠' END,
                         i)                                  AS title,
                  CASE
                      WHEN random() < 0.85 THEN format('더미 설명 %03s: 내용 예시입니다.', i)
                      ELSE NULL END                          AS description,
                  CASE
                      WHEN random() < 0.90 THEN format('https://picsum.photos/seed/c%03s/480/720', i)
                      ELSE NULL END                          AS thumbnail_url
           FROM generate_series(1, 300) AS s(i))
INSERT
INTO public.contents (average_rating, review_count, created_at, source_id, id, description, thumbnail_url, title,
                      "type")
SELECT average_rating,
       review_count,
       created_at,
       source_id,
       id,
       description,
       thumbnail_url,
       title,
       "type"
FROM c;

-- =========================
-- 4) TAGS (30개, name 유니크)
-- =========================
WITH t(name, ord) AS (SELECT *
                      FROM unnest(ARRAY [
                          'action','drama','comedy','thriller','romance','sf','fantasy','crime','mystery','documentary',
                          'korean','japanese','us','europe','classic','new','family','kids','animation','music',
                          'sports','soccer','baseball','basketball','f1','ufc','award','popular','hidden','weekend'
                          ]) WITH ORDINALITY)
INSERT
INTO public.tags (created_at, id, "name")
SELECT (now() - (random() * interval '365 days')),
       gen_random_uuid(),
       name
FROM t;

-- =========================
-- 5) CONTENT_TAGS (각 컨텐츠당 1~4개 태그)
-- =========================
INSERT INTO public.content_tags (created_at, content_id, id, tag_id)
SELECT (now() - (random() * interval '180 days')),
       c.id,
       gen_random_uuid(),
       t.id
FROM public.contents c
         JOIN LATERAL (
    SELECT id
    FROM public.tags
    ORDER BY random()
    LIMIT (1 + (random() * 3)::int) -- 1~4
    ) t ON true;

-- =========================
-- 6) FOLLOWS (약 500개, 자기 자신 제외)
-- =========================
WITH pairs AS (SELECT DISTINCT ON (follower_id, followee_id) u1.id AS follower_id,
                                                             u2.id AS followee_id
               FROM (SELECT id FROM public.users ORDER BY random() LIMIT 70) u1
                        CROSS JOIN (SELECT id FROM public.users ORDER BY random() LIMIT 70) u2
               WHERE u1.id <> u2.id
               ORDER BY follower_id, followee_id, random()
               LIMIT 500)
INSERT
INTO public.follows (created_at, followee_id, follower_id, id)
SELECT (now() - (random() * interval '200 days')),
       followee_id,
       follower_id,
       gen_random_uuid()
FROM pairs;

-- =========================
-- 7) PLAYLISTS (유저당 2개씩 = 200개)
-- =========================
WITH owners AS (SELECT id, row_number() OVER (ORDER BY created_at, id) AS rn
                FROM public.users),
     pl AS (SELECT gen_random_uuid()                                                          AS id,
                   o.id                                                                       AS owner_id,
                   (now() - (random() * interval '200 days'))                                 AS created_at,
                   (now() - (random() * interval '30 days'))                                  AS updated_at,
                   format('플레이리스트 %03s-%s', o.rn, p)                                          AS title,
                   CASE WHEN random() < 0.65 THEN format('설명 %03s-%s', o.rn, p) ELSE NULL END AS description
            FROM owners o
                     CROSS JOIN generate_series(1, 2) AS p)
INSERT
INTO public.playlists (created_at, updated_at, id, owner_id, description, title)
SELECT created_at, updated_at, id, owner_id, description, title
FROM pl;

-- =========================
-- 8) PLAYLIST_CONTENTS (플레이리스트당 10개 컨텐츠)
-- =========================
INSERT INTO public.playlist_contents (created_at, content_id, id, playlist_id)
SELECT (now() - (random() * interval '200 days')),
       c.id,
       gen_random_uuid(),
       p.id
FROM public.playlists p
         JOIN LATERAL (
    SELECT id
    FROM public.contents
    ORDER BY random()
    LIMIT 10
    ) c ON true;

-- =========================
-- 9) PLAYLIST_SUBSCRIPTIONS (플레이리스트당 0~4명 구독, owner 제외)
-- =========================
INSERT INTO public.playlist_subscriptions (created_at, id, playlist_id, user_id)
SELECT (now() - (random() * interval '200 days')),
       gen_random_uuid(),
       p.id,
       u.id
FROM public.playlists p
         JOIN LATERAL (
    SELECT id
    FROM public.users
    WHERE id <> p.owner_id
    ORDER BY random()
    LIMIT ((random() * 5)::int) -- 0~4
    ) u ON true;

-- =========================
-- 10) CONVERSATIONS / PARTICIPANTS / DIRECT_MESSAGES
--     last_message_id는 나중에 UPDATE로 채움(순환 FK 회피)
-- =========================
WITH pairs AS (SELECT DISTINCT ON (a, b) LEAST(u1.id, u2.id)    AS a,
                                         GREATEST(u1.id, u2.id) AS b
               FROM (SELECT id FROM public.users ORDER BY random() LIMIT 90) u1
                        CROSS JOIN (SELECT id FROM public.users ORDER BY random() LIMIT 90) u2
               WHERE u1.id <> u2.id
               ORDER BY a, b
               LIMIT 120),
     convs AS (SELECT gen_random_uuid()                                     AS id,
                      (now() - (random() * interval '120 days'))::timestamp AS created_at,
                      NULL::uuid                                            AS last_message_id,
                      a,
                      b
               FROM pairs),
     ins_convs AS (
         INSERT INTO public.conversations (id, created_at, last_message_id)
             SELECT id, created_at, last_message_id
             FROM convs
             RETURNING id, created_at, last_message_id),
     conv_join AS (SELECT ic.id, ic.created_at, c.a, c.b
                   FROM ins_convs ic
                            JOIN convs c USING (id)),
     ins_participants AS (
         INSERT INTO public.conversation_participants (id, created_at, user_id, conversation_id, last_read_at)
             SELECT gen_random_uuid(),
                    cj.created_at,
                    cj.a,
                    cj.id,
                    CASE
                        WHEN random() < 0.70 THEN (cj.created_at + (random() * interval '90 days'))::timestamp
                        ELSE NULL END
             FROM conv_join cj
             UNION ALL
             SELECT gen_random_uuid(),
                    cj.created_at,
                    cj.b,
                    cj.id,
                    CASE
                        WHEN random() < 0.70 THEN (cj.created_at + (random() * interval '90 days'))::timestamp
                        ELSE NULL END
             FROM conv_join cj
             RETURNING conversation_id),
     ins_messages AS (
         INSERT INTO public.direct_messages (id, conversation_id, sender_id, "content", created_at, read_at)
             SELECT gen_random_uuid(),
                    cj.id,
                    CASE WHEN (gs % 2) = 0 THEN cj.a ELSE cj.b END                  AS sender_id,
                    format('더미 DM #%s (conv %s)', gs, substring(cj.id::text, 1, 8)) AS "content",
                    (cj.created_at + (gs * interval '10 minutes'))::timestamp       AS created_at,
                    CASE
                        WHEN random() < 0.60 THEN (cj.created_at + (gs * interval '10 minutes') +
                                                   (random() * interval '2 hours'))::timestamp
                        ELSE NULL
                        END                                                         AS read_at
             FROM conv_join cj
                      JOIN LATERAL generate_series(1, (5 + (random() * 15)::int)) gs ON true
             RETURNING id, conversation_id, created_at)
UPDATE public.conversations c
SET last_message_id = x.id
FROM (SELECT DISTINCT ON (conversation_id) conversation_id,
                                           id
      FROM public.direct_messages
      ORDER BY conversation_id, created_at DESC) x
WHERE c.id = x.conversation_id;

-- =========================
-- 11) REVIEWS (컨텐츠 200개에 대해 컨텐츠당 2개 리뷰)
--     이후 contents의 average_rating / review_count를 리뷰 기반으로 현실감 있게 업데이트
-- =========================
INSERT INTO public.reviews (rating, created_at, updated_at, author_id, content_id, id, "text")
SELECT round((1 + random() * 4)::numeric, 1)::float8                                                    AS rating, -- 1.0 ~ 5.0
       (now() - (random() * interval '200 days'))                                                       AS created_at,
       CASE WHEN random() < 0.55 THEN (now() - (random() * interval '50 days')) ELSE NULL END           AS updated_at,
       u.id                                                                                             AS author_id,
       c.id                                                                                             AS content_id,
       gen_random_uuid()                                                                                AS id,
       format('더미 리뷰: content=%s, author=%s', substring(c.id::text, 1, 8), substring(u.id::text, 1, 8)) AS "text"
FROM (SELECT id FROM public.contents ORDER BY random() LIMIT 200) c
         JOIN LATERAL (
    SELECT id
    FROM public.users
    ORDER BY random()
    LIMIT 2
    ) u ON true;

WITH agg AS (SELECT content_id, avg(rating) AS avg_rating, count(*) AS cnt
             FROM public.reviews
             GROUP BY content_id)
UPDATE public.contents c
SET average_rating = round(agg.avg_rating::numeric, 2)::float8,
    review_count   = agg.cnt::int
FROM agg
WHERE c.id = agg.content_id;

-- =========================
-- 12) NOTIFICATIONS (300개, level 고르게, read_at 일부만)
-- =========================
INSERT INTO public.notifications (id, receiver_id, event_id, title, created_at, "content", "level", read_at)
SELECT gen_random_uuid(),
       u.id,
       gen_random_uuid(),
       format('알림 #%s', gs),
       (now() - (random() * interval '30 days'))::timestamp,
       format('더미 알림 내용 #%s', gs),
       CASE
           WHEN (gs % 10) = 0 THEN 'ERROR'
           WHEN (gs % 3) = 0 THEN 'WARNING'
           ELSE 'INFO'
           END,
       CASE WHEN random() < 0.50 THEN (now() - (random() * interval '30 days'))::timestamp ELSE NULL END
FROM generate_series(1, 300) gs
         JOIN LATERAL (SELECT id FROM public.users ORDER BY random() LIMIT 1) u ON true;

-- =========================
-- 13) 권한 (요청하신 라인)
-- =========================
GRANT USAGE ON SCHEMA public TO public;

COMMIT;
