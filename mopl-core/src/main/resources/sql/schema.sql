DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS playlists CASCADE;
DROP TABLE IF EXISTS playlist_subscriptions CASCADE;
DROP TABLE IF EXISTS follows CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS contents CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS content_tags CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS playlist_contents CASCADE;
DROP TABLE IF EXISTS direct_messages CASCADE;
DROP TABLE IF EXISTS conversation_participants CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;

CREATE TABLE users (
                       id UUID not null,
                       name VARCHAR not null,
                       email VARCHAR not null,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       password VARCHAR NOT NULL,
                       profile_image_url VARCHAR,
                       role VARCHAR NOT NULL,
                       locked BOOLEAN NOT NULL DEFAULT false,

                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_email UNIQUE (email), -- unique
                       CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE playlists (
                           id UUID NOT NULL,
                           owner_id UUID NOT NULL,
                           title VARCHAR NOT NULL,
                           description TEXT,
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL,

                           CONSTRAINT pk_playlists PRIMARY KEY (id),
                           CONSTRAINT fk_playlists_owner
                               FOREIGN KEY (owner_id)
                                   REFERENCES users(id)
                                   ON DELETE CASCADE
);

CREATE TABLE playlist_subscriptions (
                                        id UUID NOT NULL,
                                        playlist_id UUID NOT NULL,
                                        user_id UUID NOT NULL,
                                        created_at TIMESTAMP NOT NULL,

                                        CONSTRAINT pk_playlist_subscriptions PRIMARY KEY (id),
                                        CONSTRAINT uq_playlist_subscriptions UNIQUE (playlist_id, user_id),
                                        CONSTRAINT fk_playlist_subscriptions_playlist
                                            FOREIGN KEY (playlist_id)
                                                REFERENCES playlists(id)
                                                ON DELETE CASCADE,
                                        CONSTRAINT fk_playlist_subscriptions_user
                                            FOREIGN KEY (user_id)
                                                REFERENCES users(id)
                                                ON DELETE CASCADE
);

CREATE TABLE follows (
                         id UUID NOT NULL,
                         follower_id UUID NOT NULL,
                         followee_id UUID NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT pk_follows PRIMARY KEY (id),
                         CONSTRAINT uq_follows UNIQUE (follower_id, followee_id),
                         CONSTRAINT ck_follows_no_self CHECK (follower_id <> followee_id), -- 자기 자신 X

                         CONSTRAINT fk_follows_follower
                             FOREIGN KEY (follower_id)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT fk_follows_followee
                             FOREIGN KEY (followee_id)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE
);

CREATE TABLE contents (
                          id UUID NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          type VARCHAR NOT NULL,
                          title VARCHAR NOT NULL,
                          description TEXT NULL,
                          thumbnail_url VARCHAR NULL,
                          average_rating DOUBLE precision NOT NULL DEFAULT 0,
                          review_count INT NOT NULL DEFAULT 0,

                          CONSTRAINT pk_contents PRIMARY KEY (id),
                          CONSTRAINT ck_contents_rating CHECK (average_rating >= 0 AND average_rating <= 5),
                          CONSTRAINT ck_contents_review_count CHECK (review_count >= 0)
);

-- reviews 테이블 생성 (contents FK 포함)
CREATE TABLE reviews (
                         id UUID NOT NULL,
                         author_id UUID NOT NULL,
                         content_id UUID NOT NULL,
                         text TEXT NOT NULL,
                         rating DOUBLE precision NOT NULL,
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP NULL,

                         CONSTRAINT pk_reviews PRIMARY KEY (id),
                         CONSTRAINT ck_reviews_rating CHECK (rating >= 1 AND rating <= 5),

                         CONSTRAINT fk_reviews_author
                             FOREIGN KEY (author_id)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT fk_reviews_content
                             FOREIGN KEY (content_id)
                                 REFERENCES contents(id)
                                 ON DELETE CASCADE
);

CREATE TABLE tags (
                      id UUID NOT NULL,
                      name VARCHAR NOT NULL,
                      created_at TIMESTAMP NOT NULL,

                      CONSTRAINT pk_tags PRIMARY KEY (id),
                      CONSTRAINT uq_tags_name UNIQUE (name)
);

CREATE TABLE content_tags (
                              id UUID NOT NULL,
                              content_id UUID NOT NULL,
                              tag_id UUID NOT NULL,
                              created_at TIMESTAMP NOT NULL,


                              CONSTRAINT pk_content_tags PRIMARY KEY (id),
                              CONSTRAINT uq_content_tag UNIQUE (content_id, tag_id),
                              CONSTRAINT fk_content_tags_content
                                  FOREIGN KEY (content_id)
                                      REFERENCES contents(id)
                                      ON DELETE CASCADE,
                              CONSTRAINT fk_content_tags_tag
                                  FOREIGN KEY (tag_id)
                                      REFERENCES tags(id)
                                      ON DELETE CASCADE
);

CREATE TABLE notifications (
                               id UUID NOT NULL,
                               receiver_id UUID NOT NULL,
                               title VARCHAR NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               content TEXT NOT NULL,
                               level VARCHAR NOT NULL,
                               read_at TIMESTAMP NULL,

                               CONSTRAINT pk_notifications PRIMARY KEY (id),

                               CONSTRAINT fk_notifications_receiver
                                   FOREIGN KEY (receiver_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);

CREATE TABLE playlist_contents (
                                   id UUID NOT NULL,
                                   playlist_id UUID NOT NULL,
                                   content_id UUID NOT NULL,
                                   created_at TIMESTAMP NOT NULL,

                                   CONSTRAINT pk_playlist_contents PRIMARY KEY (id),
                                   CONSTRAINT uq_playlist_contents UNIQUE (playlist_id, content_id),
                                   CONSTRAINT fk_playlist_contents_playlist
                                       FOREIGN KEY (playlist_id)
                                           REFERENCES playlists(id)
                                           ON DELETE CASCADE,
                                   CONSTRAINT fk_playlist_contents_content
                                       FOREIGN KEY (content_id)
                                           REFERENCES contents(id)
                                           ON DELETE CASCADE
);

CREATE TABLE conversations (
                               id UUID NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               last_message_id UUID NULL,

                               CONSTRAINT pk_conversations PRIMARY KEY (id)
);

CREATE TABLE conversation_participants (
                                           id UUID NOT NULL,
                                           created_at TIMESTAMP NOT NULL,
                                           user_id UUID NOT NULL,
                                           conversation_id UUID NOT NULL,
                                           last_read_at TIMESTAMP NULL,

                                           CONSTRAINT pk_conversation_participants PRIMARY KEY (id),
                                           CONSTRAINT fk_conv_participants_user
                                               FOREIGN KEY (user_id)
                                                   REFERENCES users(id)
                                                   ON DELETE CASCADE,
                                           CONSTRAINT fk_conv_participants_conversation
                                               FOREIGN KEY (conversation_id)
                                                   REFERENCES conversations(id)
                                                   ON DELETE CASCADE,

                                           CONSTRAINT uq_conv_participants UNIQUE (conversation_id, user_id)
);

CREATE TABLE direct_messages (
                                 id UUID NOT NULL,
                                 conversation_id UUID NOT NULL,
                                 sender_id UUID NOT NULL,
                                 content TEXT NOT NULL,
                                 created_at TIMESTAMP NOT NULL,

                                 CONSTRAINT pk_direct_messages PRIMARY KEY (id),
                                 CONSTRAINT fk_dm_conversation
                                     FOREIGN KEY (conversation_id)
                                         REFERENCES conversations(id)
                                         ON DELETE CASCADE,
                                 CONSTRAINT fk_dm_sender
                                     FOREIGN KEY (sender_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE
);

ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_last_message
        FOREIGN KEY (last_message_id)
            REFERENCES direct_messages(id)
            ON DELETE SET NULL;


CREATE INDEX IF NOT EXISTS idx_playlists_owner_id
    ON playlists(owner_id);

CREATE INDEX IF NOT EXISTS idx_playlist_subscriptions_user_id
    ON playlist_subscriptions(user_id);

CREATE INDEX IF NOT EXISTS idx_follows_followee_id
    ON follows(followee_id);

CREATE INDEX IF NOT EXISTS idx_reviews_author_id
    ON reviews(author_id);

CREATE INDEX IF NOT EXISTS idx_reviews_content_id
    ON reviews(content_id);

CREATE INDEX IF NOT EXISTS idx_content_tags_tag_id
    ON content_tags(tag_id);

CREATE INDEX IF NOT EXISTS idx_notifications_receiver_id
    ON notifications(receiver_id);

CREATE INDEX IF NOT EXISTS idx_playlist_contents_content_id
    ON playlist_contents(content_id);

CREATE INDEX IF NOT EXISTS idx_conversation_participants_user_id
    ON conversation_participants(user_id);

CREATE INDEX IF NOT EXISTS idx_direct_messages_conversation_id
    ON direct_messages(conversation_id);

CREATE INDEX IF NOT EXISTS idx_direct_messages_sender_id
    ON direct_messages(sender_id);
