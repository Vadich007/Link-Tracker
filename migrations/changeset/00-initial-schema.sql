--changeset maksim:1
CREATE TABLE users (
    chat_id BIGINT PRIMARY KEY,
    state VARCHAR(30) NOT NULL,
    add_link_request VARCHAR
);

CREATE TABLE chats (
    chat_id BIGINT PRIMARY KEY
);

CREATE TABLE links (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR NOT NULL,
    type VARCHAR(30) NOT NULL,
    last_event VARCHAR
);

CREATE TABLE subscriptions (
    chat_id BIGINT NOT NULL,
    link_id BIGINT NOT NULL,
    tags VARCHAR,
    filters VARCHAR
);

