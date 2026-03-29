-- liquibase formatted sql

-- changeset ai:user-avatar-url-01
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(1000) NULL;
