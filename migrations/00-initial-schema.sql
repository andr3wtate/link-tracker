--liquibase formatted sql

--changeset andrew:1
CREATE TABLE IF NOT EXISTS bot_users (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--changeset andrew:2
CREATE TABLE IF NOT EXISTS bot_users_sessions (
    chat_id BIGINT PRIMARY KEY REFERENCES bot_users(chat_id) ON DELETE CASCADE,
    chat_state VARCHAR(20) NOT NULL,
    arguments TEXT[] NOT NULL DEFAULT '{}',
    tags TEXT[] NOT NULL DEFAULT '{}',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--changeset andrew:3
-- Пока что так, если понадобится, потом можно добавить уникальный кэш
CREATE TABLE IF NOT EXISTS uri_string_cache (
    uri TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--changeset andrew:4
-- Пока что так, если понадобится, потом можно добавить уникальный кэш
CREATE TABLE IF NOT EXISTS string_long_cache (
    key TEXT PRIMARY KEY,
    value BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--changeset andrew:5
CREATE TABLE IF NOT EXISTS scrapper_users (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--changeset andrew:6
CREATE TABLE IF NOT EXISTS links (
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL UNIQUE,
    last_check TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--changeset andrew:7
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES scrapper_users(id) ON DELETE CASCADE,
    link_id BIGINT NOT NULL REFERENCES links(id) ON DELETE CASCADE,
    tags TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_link UNIQUE (user_id, link_id)
);

--changeset andrew:8
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_link_id ON subscriptions(link_id);
CREATE INDEX IF NOT EXISTS idx_bot_users_chat_id ON bot_users(chat_id);
CREATE INDEX IF NOT EXISTS idx_scrapper_users_chat_id ON scrapper_users(chat_id);
