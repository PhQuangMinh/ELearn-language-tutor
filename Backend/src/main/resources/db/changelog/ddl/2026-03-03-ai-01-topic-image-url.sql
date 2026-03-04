-- liquibase formatted sql

-- changeset ai:topic-image-url-01
ALTER TABLE topics
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(1000) NULL;

