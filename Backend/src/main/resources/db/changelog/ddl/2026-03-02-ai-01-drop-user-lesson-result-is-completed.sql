-- liquibase formatted sql

-- changeset ai:drop-user-lesson-result-is-completed-01
ALTER TABLE user_lesson_results
    DROP COLUMN is_completed;

