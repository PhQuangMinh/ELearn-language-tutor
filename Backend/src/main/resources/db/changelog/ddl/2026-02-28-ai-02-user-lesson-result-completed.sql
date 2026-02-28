-- liquibase formatted sql

-- changeset ai:user-lesson-result-is-completed-01
ALTER TABLE user_lesson_results
    ADD COLUMN IF NOT EXISTS is_completed BOOLEAN NOT NULL DEFAULT FALSE;

