-- liquibase formatted sql

-- changeset ai:lesson-image-url-01
ALTER TABLE lessons
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(1000) NULL;

