-- liquibase formatted sql

-- changeset minhpq:scenarios-topic-to-lesson-02
-- 1) Add missing columns to scenarios (if they don't exist yet)
ALTER TABLE scenarios
    ADD COLUMN IF NOT EXISTS description VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS tasks VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS openning_message VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS suggestion VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS translation VARCHAR(500) NULL;

-- 2) Add lesson_id and migrate existing scenarios' topic_id -> lesson_id
ALTER TABLE scenarios
    ADD COLUMN IF NOT EXISTS lesson_id INT NULL;

UPDATE scenarios s
SET s.lesson_id = (
    SELECT MIN(l.id)
    FROM lessons l
    WHERE l.topic_id = s.topic_id
);

-- 3) Change the FK/relationship from topics -> scenarios to lessons -> scenarios
-- Add new FK for lesson_id
ALTER TABLE scenarios
    ADD CONSTRAINT scenarios_lessons_id_fk
        FOREIGN KEY (lesson_id) REFERENCES lessons (id);

-- Drop FK for topic_id (keep column for backward compatibility; application code should stop using it)
ALTER TABLE scenarios
    DROP FOREIGN KEY scenarios_topics_id_fk;

