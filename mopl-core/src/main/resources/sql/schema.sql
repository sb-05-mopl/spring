-- DROP SCHEMA public;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP SEQUENCE IF EXISTS public.batch_job_execution_seq CASCADE;

CREATE SEQUENCE public.batch_job_execution_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1
    NO CYCLE;

-- Permissions

ALTER SEQUENCE public.batch_job_execution_seq OWNER TO app;
GRANT ALL ON SEQUENCE public.batch_job_execution_seq TO app;

DROP SEQUENCE IF EXISTS public.batch_job_seq CASCADE;

CREATE SEQUENCE public.batch_job_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1
    NO CYCLE;

-- Permissions

ALTER SEQUENCE public.batch_job_seq OWNER TO app;
GRANT ALL ON SEQUENCE public.batch_job_seq TO app;

DROP SEQUENCE IF EXISTS public.batch_step_execution_seq CASCADE;

CREATE SEQUENCE public.batch_step_execution_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1
    NO CYCLE;

-- Permissions

ALTER SEQUENCE public.batch_step_execution_seq OWNER TO app;
GRANT ALL ON SEQUENCE public.batch_step_execution_seq TO app;
-- public.contents definition

-- Drop table

DROP TABLE IF EXISTS public.contents CASCADE;

CREATE TABLE public.contents
(
    average_rating float8         NOT NULL,
    review_count   int4           NOT NULL,
    created_at     timestamptz(6) NOT NULL,
    source_id      int8           NOT NULL,
    id             uuid           NOT NULL,
    description    text           NULL,
    thumbnail_url  varchar(255)   NULL,
    title          varchar(255)   NOT NULL,
    "type"         varchar(255)   NOT NULL,
    CONSTRAINT contents_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.contents
    OWNER TO app;
GRANT ALL ON TABLE public.contents TO app;


-- public.notifications definition

-- Drop table

DROP TABLE IF EXISTS public.notifications CASCADE;

CREATE TABLE public.notifications
(
    id          uuid      NOT NULL,
    receiver_id uuid      NOT NULL,
    event_id    uuid      NOT NULL,
    title       varchar   NOT NULL,
    created_at  timestamp NOT NULL,
    "content"   text      NOT NULL,
    "level"     varchar   NOT NULL,
    read_at     timestamp NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT uk_notifications_event_id UNIQUE (event_id)
);
CREATE INDEX idx_notifications_receiver_id ON public.notifications (receiver_id);
CREATE INDEX idx_notifications_event_id ON public.notifications (event_id);

-- Permissions

ALTER TABLE public.notifications
    OWNER TO app;
GRANT ALL ON TABLE public.notifications TO app;


-- public.tags definition

-- Drop table

DROP TABLE IF EXISTS public.tags CASCADE;

CREATE TABLE public.tags
(
    created_at timestamptz(6) NOT NULL,
    id         uuid           NOT NULL,
    "name"     varchar(255)   NOT NULL,
    CONSTRAINT tags_name_key UNIQUE ("name"),
    CONSTRAINT tags_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.tags
    OWNER TO app;
GRANT ALL ON TABLE public.tags TO app;


-- public.users definition

-- Drop table

DROP TABLE IF EXISTS public.users CASCADE;

CREATE TABLE public.users
(
    "locked"          bool           NOT NULL,
    created_at        timestamptz(6) NOT NULL,
    id                uuid           NOT NULL,
    email             varchar(255)   NOT NULL,
    "name"            varchar(255)   NOT NULL,
    "password"        varchar(255)   NULL,
    profile_image_url varchar(255)   NULL,
    provider          varchar(255)   NOT NULL,
    "role"            varchar(255)   NOT NULL,
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_provider_check CHECK (((provider)::text = ANY
                                            ((ARRAY ['LOCAL'::character varying, 'GOOGLE'::character varying, 'KAKAO'::character varying])::text[]))),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY
                                        ((ARRAY ['USER'::character varying, 'ADMIN'::character varying])::text[])))
);

-- Permissions

ALTER TABLE public.users
    OWNER TO app;
GRANT ALL ON TABLE public.users TO app;


-- public.content_tags definition

-- Drop table

DROP TABLE IF EXISTS public.content_tags CASCADE;

CREATE TABLE public.content_tags
(
    created_at timestamptz(6) NOT NULL,
    content_id uuid           NOT NULL,
    id         uuid           NOT NULL,
    tag_id     uuid           NOT NULL,
    CONSTRAINT content_tags_pkey PRIMARY KEY (id),
    CONSTRAINT fkcr5rprg24ccjyjsnc7cac859f FOREIGN KEY (tag_id) REFERENCES public.tags (id),
    CONSTRAINT fkq97jtguli56lt3e73h32jbwxy FOREIGN KEY (content_id) REFERENCES public.contents (id)
);

-- Permissions

ALTER TABLE public.content_tags
    OWNER TO app;
GRANT ALL ON TABLE public.content_tags TO app;


-- public.follows definition

-- Drop table

DROP TABLE IF EXISTS public.follows CASCADE;

CREATE TABLE public.follows
(
    created_at  timestamptz(6) NOT NULL,
    followee_id uuid           NOT NULL,
    follower_id uuid           NOT NULL,
    id          uuid           NOT NULL,
    CONSTRAINT follows_pkey PRIMARY KEY (id),
    CONSTRAINT fkeo7hqi2bt2vdwk6mpu0w2ihyb FOREIGN KEY (followee_id) REFERENCES public.users (id),
    CONSTRAINT fkqnkw0cwwh6572nyhvdjqlr163 FOREIGN KEY (follower_id) REFERENCES public.users (id)
);

-- Permissions

ALTER TABLE public.follows
    OWNER TO app;
GRANT ALL ON TABLE public.follows TO app;


-- public.playlists definition

-- Drop table

DROP TABLE IF EXISTS public.playlists CASCADE;

CREATE TABLE public.playlists
(
    created_at  timestamptz(6) NOT NULL,
    updated_at  timestamptz(6) NOT NULL,
    id          uuid           NOT NULL,
    owner_id    uuid           NOT NULL,
    description text           NULL,
    title       varchar(255)   NOT NULL,
    CONSTRAINT playlists_pkey PRIMARY KEY (id),
    CONSTRAINT fkn0r1rrm19f8j7wfl656bd9sus FOREIGN KEY (owner_id) REFERENCES public.users (id)
);

-- Permissions

ALTER TABLE public.playlists
    OWNER TO app;
GRANT ALL ON TABLE public.playlists TO app;


-- public.reviews definition

-- Drop table

DROP TABLE IF EXISTS public.reviews CASCADE;

CREATE TABLE public.reviews
(
    rating     float8         NOT NULL,
    created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NULL,
    author_id  uuid           NOT NULL,
    content_id uuid           NOT NULL,
    id         uuid           NOT NULL,
    "text"     text           NOT NULL,
    CONSTRAINT reviews_pkey PRIMARY KEY (id),
    CONSTRAINT fkohsyiuxht0o3qx6hmtytxdyo4 FOREIGN KEY (content_id) REFERENCES public.contents (id),
    CONSTRAINT fkse5kx11600wtv0jh9jobvrdpi FOREIGN KEY (author_id) REFERENCES public.users (id)
);

-- Permissions

ALTER TABLE public.reviews
    OWNER TO app;
GRANT ALL ON TABLE public.reviews TO app;


-- public.playlist_contents definition

-- Drop table

DROP TABLE IF EXISTS public.playlist_contents CASCADE;

CREATE TABLE public.playlist_contents
(
    created_at  timestamptz(6) NOT NULL,
    content_id  uuid           NOT NULL,
    id          uuid           NOT NULL,
    playlist_id uuid           NOT NULL,
    CONSTRAINT playlist_contents_pkey PRIMARY KEY (id),
    CONSTRAINT fkjsc6gyglh0w7fn83movab0lq1 FOREIGN KEY (content_id) REFERENCES public.contents (id),
    CONSTRAINT fknjuw6x7c4ys2l627bl1xocdc9 FOREIGN KEY (playlist_id) REFERENCES public.playlists (id)
);

-- Permissions

ALTER TABLE public.playlist_contents
    OWNER TO app;
GRANT ALL ON TABLE public.playlist_contents TO app;


-- public.playlist_subscriptions definition

-- Drop table

DROP TABLE IF EXISTS public.playlist_subscriptions CASCADE;

CREATE TABLE public.playlist_subscriptions
(
    created_at  timestamptz(6) NOT NULL,
    id          uuid           NOT NULL,
    playlist_id uuid           NOT NULL,
    user_id     uuid           NOT NULL,
    CONSTRAINT playlist_subscriptions_pkey PRIMARY KEY (id),
    CONSTRAINT fkdf1h4h7vqlvygg4uia944hhk6 FOREIGN KEY (playlist_id) REFERENCES public.playlists (id),
    CONSTRAINT fkoh6gfn5s6sto9b9cxios0cy24 FOREIGN KEY (user_id) REFERENCES public.users (id)
);

-- Permissions

ALTER TABLE public.playlist_subscriptions
    OWNER TO app;
GRANT ALL ON TABLE public.playlist_subscriptions TO app;


-- public.conversation_participants definition

-- Drop table

DROP TABLE IF EXISTS public.conversation_participants CASCADE;

CREATE TABLE public.conversation_participants
(
    id              uuid      NOT NULL,
    created_at      timestamp NOT NULL,
    user_id         uuid      NOT NULL,
    conversation_id uuid      NOT NULL,
    last_read_at    timestamp NULL,
    CONSTRAINT pk_conversation_participants PRIMARY KEY (id),
    CONSTRAINT uq_conv_participants UNIQUE (conversation_id, user_id)
);
CREATE INDEX idx_conversation_participants_user_id ON public.conversation_participants (user_id);

-- Permissions

ALTER TABLE public.conversation_participants
    OWNER TO app;
GRANT ALL ON TABLE public.conversation_participants TO app;


-- public.conversations definition

-- Drop table

DROP TABLE IF EXISTS public.conversations CASCADE;

CREATE TABLE public.conversations
(
    id              uuid      NOT NULL,
    created_at      timestamp NOT NULL,
    last_message_id uuid      NULL,
    CONSTRAINT pk_conversations PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.conversations
    OWNER TO app;
GRANT ALL ON TABLE public.conversations TO app;


-- public.direct_messages definition

-- Drop table

DROP TABLE IF EXISTS public.direct_messages CASCADE;

CREATE TABLE public.direct_messages
(
    id              uuid      NOT NULL,
    conversation_id uuid      NOT NULL,
    sender_id       uuid      NOT NULL,
    "content"       text      NOT NULL,
    created_at      timestamp NOT NULL,
    read_at         timestamp NULL,
    CONSTRAINT pk_direct_messages PRIMARY KEY (id)
);
CREATE INDEX idx_direct_messages_conversation_id ON public.direct_messages (conversation_id uuid_ops);
CREATE INDEX idx_direct_messages_sender_id ON public.direct_messages (sender_id uuid_ops);

-- Permissions

ALTER TABLE public.direct_messages
    OWNER TO app;
GRANT ALL ON TABLE public.direct_messages TO app;


-- public.conversation_participants foreign keys

ALTER TABLE public.conversation_participants
    ADD CONSTRAINT fk_conv_participants_conversation FOREIGN KEY (conversation_id) REFERENCES public.conversations (id) ON DELETE CASCADE;


-- public.conversations foreign keys

ALTER TABLE public.conversations
    ADD CONSTRAINT fk_conversations_last_message FOREIGN KEY (last_message_id) REFERENCES public.direct_messages (id) ON DELETE SET NULL;


-- public.direct_messages foreign keys

ALTER TABLE public.direct_messages
    ADD CONSTRAINT fk_dm_conversation FOREIGN KEY (conversation_id) REFERENCES public.conversations (id) ON DELETE CASCADE;


-- DROP FUNCTION public.armor(bytea);

CREATE OR REPLACE FUNCTION public.armor(bytea)
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_armor$function$
;

-- Permissions

ALTER FUNCTION public.armor(bytea) OWNER TO app;
GRANT ALL ON FUNCTION public.armor(bytea) TO app;

-- DROP FUNCTION public.armor(bytea, _text, _text);

CREATE OR REPLACE FUNCTION public.armor(bytea, text[], text[])
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_armor$function$
;

-- Permissions

ALTER FUNCTION public.armor(bytea, _text, _text) OWNER TO app;
GRANT ALL ON FUNCTION public.armor(bytea, _text, _text) TO app;

-- DROP FUNCTION public.crypt(text, text);

CREATE OR REPLACE FUNCTION public.crypt(text, text)
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_crypt$function$
;

-- Permissions

ALTER FUNCTION public.crypt(text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.crypt(text, text) TO app;

-- DROP FUNCTION public.dearmor(text);

CREATE OR REPLACE FUNCTION public.dearmor(text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_dearmor$function$
;

-- Permissions

ALTER FUNCTION public.dearmor(text) OWNER TO app;
GRANT ALL ON FUNCTION public.dearmor(text) TO app;

-- DROP FUNCTION public.decrypt(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.decrypt(bytea, bytea, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_decrypt$function$
;

-- Permissions

ALTER FUNCTION public.decrypt(bytea, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.decrypt(bytea, bytea, text) TO app;

-- DROP FUNCTION public.decrypt_iv(bytea, bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.decrypt_iv(bytea, bytea, bytea, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_decrypt_iv$function$
;

-- Permissions

ALTER FUNCTION public.decrypt_iv(bytea, bytea, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.decrypt_iv(bytea, bytea, bytea, text) TO app;

-- DROP FUNCTION public.digest(bytea, text);

CREATE OR REPLACE FUNCTION public.digest(bytea, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_digest$function$
;

-- Permissions

ALTER FUNCTION public.digest(bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.digest(bytea, text) TO app;

-- DROP FUNCTION public.digest(text, text);

CREATE OR REPLACE FUNCTION public.digest(text, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_digest$function$
;

-- Permissions

ALTER FUNCTION public.digest(text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.digest(text, text) TO app;

-- DROP FUNCTION public.encrypt(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.encrypt(bytea, bytea, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_encrypt$function$
;

-- Permissions

ALTER FUNCTION public.encrypt(bytea, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.encrypt(bytea, bytea, text) TO app;

-- DROP FUNCTION public.encrypt_iv(bytea, bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.encrypt_iv(bytea, bytea, bytea, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_encrypt_iv$function$
;

-- Permissions

ALTER FUNCTION public.encrypt_iv(bytea, bytea, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.encrypt_iv(bytea, bytea, bytea, text) TO app;

-- DROP FUNCTION public.gen_random_bytes(int4);

CREATE OR REPLACE FUNCTION public.gen_random_bytes(integer)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_random_bytes$function$
;

-- Permissions

ALTER FUNCTION public.gen_random_bytes(int4) OWNER TO app;
GRANT ALL ON FUNCTION public.gen_random_bytes(int4) TO app;

-- DROP FUNCTION public.gen_random_uuid();

CREATE OR REPLACE FUNCTION public.gen_random_uuid()
    RETURNS uuid
    LANGUAGE c
    PARALLEL SAFE
AS
'$libdir/pgcrypto',
$function$pg_random_uuid$function$
;

-- Permissions

ALTER FUNCTION public.gen_random_uuid() OWNER TO app;
GRANT ALL ON FUNCTION public.gen_random_uuid() TO app;

-- DROP FUNCTION public.gen_salt(text, int4);

CREATE OR REPLACE FUNCTION public.gen_salt(text, integer)
    RETURNS text
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_gen_salt_rounds$function$
;

-- Permissions

ALTER FUNCTION public.gen_salt(text, int4) OWNER TO app;
GRANT ALL ON FUNCTION public.gen_salt(text, int4) TO app;

-- DROP FUNCTION public.gen_salt(text);

CREATE OR REPLACE FUNCTION public.gen_salt(text)
    RETURNS text
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_gen_salt$function$
;

-- Permissions

ALTER FUNCTION public.gen_salt(text) OWNER TO app;
GRANT ALL ON FUNCTION public.gen_salt(text) TO app;

-- DROP FUNCTION public.hmac(text, text, text);

CREATE OR REPLACE FUNCTION public.hmac(text, text, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_hmac$function$
;

-- Permissions

ALTER FUNCTION public.hmac(text, text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.hmac(text, text, text) TO app;

-- DROP FUNCTION public.hmac(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.hmac(bytea, bytea, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pg_hmac$function$
;

-- Permissions

ALTER FUNCTION public.hmac(bytea, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.hmac(bytea, bytea, text) TO app;

-- DROP FUNCTION public.pgp_armor_headers(in text, out text, out text);

CREATE OR REPLACE FUNCTION public.pgp_armor_headers(text, OUT key text, OUT value text)
    RETURNS SETOF record
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_armor_headers$function$
;

-- Permissions

ALTER FUNCTION public.pgp_armor_headers(in text, out text, out text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_armor_headers(in text, out text, out text) TO app;

-- DROP FUNCTION public.pgp_key_id(bytea);

CREATE OR REPLACE FUNCTION public.pgp_key_id(bytea)
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_key_id_w$function$
;

-- Permissions

ALTER FUNCTION public.pgp_key_id(bytea) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_key_id(bytea) TO app;

-- DROP FUNCTION public.pgp_pub_decrypt(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea, text)
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_decrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_decrypt(bytea, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_decrypt(bytea, bytea, text) TO app;

-- DROP FUNCTION public.pgp_pub_decrypt(bytea, bytea);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea)
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_decrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_decrypt(bytea, bytea) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_decrypt(bytea, bytea) TO app;

-- DROP FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text)
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_decrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_decrypt(bytea, bytea, text, text) TO app;

-- DROP FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_decrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text) TO app;

-- DROP FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_decrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea, text, text) TO app;

-- DROP FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea);

CREATE OR REPLACE FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_decrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_decrypt_bytea(bytea, bytea) TO app;

-- DROP FUNCTION public.pgp_pub_encrypt(text, bytea);

CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt(text, bytea)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_encrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_encrypt(text, bytea) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_encrypt(text, bytea) TO app;

-- DROP FUNCTION public.pgp_pub_encrypt(text, bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt(text, bytea, text)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_encrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_encrypt(text, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_encrypt(text, bytea, text) TO app;

-- DROP FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_encrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea, text) TO app;

-- DROP FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea);

CREATE OR REPLACE FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_pub_encrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_pub_encrypt_bytea(bytea, bytea) TO app;

-- DROP FUNCTION public.pgp_sym_decrypt(bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt(bytea, text)
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_sym_decrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_sym_decrypt(bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_sym_decrypt(bytea, text) TO app;

-- DROP FUNCTION public.pgp_sym_decrypt(bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt(bytea, text, text)
    RETURNS text
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_sym_decrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_sym_decrypt(bytea, text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_sym_decrypt(bytea, text, text) TO app;

-- DROP FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_sym_decrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_sym_decrypt_bytea(bytea, text, text) TO app;

-- DROP FUNCTION public.pgp_sym_decrypt_bytea(bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_decrypt_bytea(bytea, text)
    RETURNS bytea
    LANGUAGE c
    IMMUTABLE PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_sym_decrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_sym_decrypt_bytea(bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_sym_decrypt_bytea(bytea, text) TO app;

-- DROP FUNCTION public.pgp_sym_encrypt(text, text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt(text, text, text)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_sym_encrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_sym_encrypt(text, text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_sym_encrypt(text, text, text) TO app;

-- DROP FUNCTION public.pgp_sym_encrypt(text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt(text, text)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_sym_encrypt_text$function$
;

-- Permissions

ALTER FUNCTION public.pgp_sym_encrypt(text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_sym_encrypt(text, text) TO app;

-- DROP FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_sym_encrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_sym_encrypt_bytea(bytea, text, text) TO app;

-- DROP FUNCTION public.pgp_sym_encrypt_bytea(bytea, text);

CREATE OR REPLACE FUNCTION public.pgp_sym_encrypt_bytea(bytea, text)
    RETURNS bytea
    LANGUAGE c
    PARALLEL SAFE STRICT
AS
'$libdir/pgcrypto',
$function$pgp_sym_encrypt_bytea$function$
;

-- Permissions

ALTER FUNCTION public.pgp_sym_encrypt_bytea(bytea, text) OWNER TO app;
GRANT ALL ON FUNCTION public.pgp_sym_encrypt_bytea(bytea, text) TO app;


-- Permissions

GRANT ALL ON SCHEMA public TO pg_database_owner;
GRANT USAGE ON SCHEMA public TO public;